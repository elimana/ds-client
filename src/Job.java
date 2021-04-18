/**
 * Constructs and provides interfaces for the job object
 */
public class Job {
  private int ID, submitTime, estRuntime, core, memory, disk;

  /**
   * Constructs a job object from string
   * 
   * @param s Constructor input string with job fields delimited by the space
   *          character
   */
  public Job(String s) {
    String[] parsed = s.split(" ");
    submitTime = Integer.parseInt(parsed[1]);
    ID = Integer.parseInt(parsed[2]);
    estRuntime = Integer.parseInt(parsed[3]);
    core = Integer.parseInt(parsed[4]);
    memory = Integer.parseInt(parsed[5]);
    disk = Integer.parseInt(parsed[6]);
  }

  /**
   * Returns better formatted job ID, for testing purposes
   * 
   * @return Better formatted job ID in string format
   */
  @Override
  public String toString() {
    return "JOBN " + ID;
  }

  /**
   * Getter function for job submit time
   * 
   * @return job submit time in int format
   */
  public int getSubmitTime() {
    return this.submitTime;
  }

  /**
   * Getter function for job ID
   * 
   * @return job ID in simpler int format
   */
  public int getID() {
    return this.ID;
  }

  /**
   * Getter function for job estimated runtime
   * 
   * @return job estimated runtime in int format
   */
  public int getEstRuntime() {
    return this.estRuntime;
  }

  /**
   * Getter function for the job's core count requirement
   * 
   * @return required core count in int format
   */
  public int getCore() {
    return this.core;
  }

  /**
   * Getter function for the job's memory requirement
   * 
   * @return required memory to run this job (MB) in int format
   */
  public int getMemory() {
    return this.memory;
  }

  /**
   * Getter function for the job's disk space requirement
   * 
   * @return required disk space to run this job (MB) in int format
   */
  public int getDisk() {
    return this.disk;
  }
}
