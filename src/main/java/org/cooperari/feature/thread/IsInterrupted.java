package org.cooperari.feature.thread;


import org.cooperari.core.CThread;

/**
 * Cooperative operation for {@link Thread#isInterrupted()}.
 * 
 * @since 0.2
 */
public final class IsInterrupted extends ThreadOperation<Boolean> {
  /**
   * Thread to query interruption.
   */
  private final CThread _thread;

  /**
   * Interruption status result.
   */
  private boolean _interrupted;

  /**
   * Constructor.
   * @param thread Thread to interrupt.
   */ 
  IsInterrupted(CThread thread) {
    super(thread);
    _thread = thread;
  }

  /**
   * Complete by obtaining interruption status.
   */
  @Override
  public void execute() {
    _interrupted = _thread.getInterruptStatus();
  }

  /**
   * Get interruption status. 
   * @return A <code>Boolean</code> object.
   */
  public Boolean getResult() {
    return _interrupted;
  }

  /**
   * Execute an operation of this kind.
   * @param thisThread This thread.
   * @param otherThread Thread to query.
   * @return Interruption status for <code>otherThread</code>.
   */
  public static boolean execute(CThread thisThread, Thread otherThread) {
    return thisThread.cYield(new IsInterrupted(CThreadFeature.getCThread(otherThread)));
  }
}