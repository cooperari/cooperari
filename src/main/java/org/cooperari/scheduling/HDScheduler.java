package org.cooperari.scheduling;

import java.util.List;
import java.util.HashSet;
import java.util.Random;

import org.cooperari.core.CWorkspace;
import org.cooperari.core.util.CRawTuple;


/**
 * History-dependent scheduler.
 * 
 * <p>
 * This scheduler is an improvement over the memoryless scheduler.
 * It tries to avoid repeated scheduling decisions whenever possible. 
 * It starts by trying a random choice and checks if it corresponds
 * to a previous decision for the same program state. 
 * If the choice is a repeated decision, the scheduler
 * then iterates the remaining program's state elements, 
 * looking for a decision that has not been made.
 * </p>
 * 
 * @since 0.2
 */
public final class HDScheduler extends CScheduler {

  /**
   * Pseudo-random number generator.
   */
  private Random _rng;

  /**
   * Log of previous decisions.
   */
  private final HashSet<CRawTuple> _log = new HashSet<>();

  private int _prevLogSize = 0;

  /**
   * Constructor.
   */
  public HDScheduler() {
    _rng = new Random(0); // a fixed seed is used for repeatable tests
    CWorkspace.log("HDSCH");

  }
  @Override
  public void onTestStarted() {
    _prevLogSize = _log.size();
  }


  @Override
  public void onTestFailure(Throwable failure) {
CWorkspace.log("history: %d -> %d", _prevLogSize, _log.size());
  }

  /*
   * (non-Javadoc)
   * @see org.cooperari.core.CoveragePolicy#onTestFinished()
   */
  @Override
  public void onTestFinished() {
    CWorkspace.log("history: %d -> %d", _prevLogSize, _log.size());
  }
  
  
  /**
   * Check if further trials are necessary.
   * Further trials will be required if new decisions were recorded for the last one.
   * @return <code>true</code> if another test trial is required.
   */
  @Override
  public boolean continueTrials() {
    CWorkspace.log("history: %d -> %d", _prevLogSize, _log.size());
    return  _prevLogSize != _log.size();
  }
  
  /**
   * Select the next thread to run.
   * @param state Program state.
   */
  @Override
  public CThreadHandle decision(CProgramState state) {
    final Object sig = state.getSignature();
    final List<? extends CProgramState.CElement> possibleChoices = state.readyElements();
    final int n = possibleChoices.size();
    final int firstChoice = _rng.nextInt(n);
    int choice = firstChoice;
    int tries = 0;
    CRawTuple d;
    CThreadHandle t;
    
    do {
      t = state.select(choice, _rng);
      d = new CRawTuple(sig, choice);
      choice = (choice + 1) % n;
      tries++;
      CWorkspace.log(d.toString());
    } while (!_log.add(d) && tries < possibleChoices.size());
    return t;
  }
}