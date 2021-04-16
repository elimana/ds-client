public class Job {
  private int ID, submitTime, estRuntime, core, memory, disk;

  public Job(String s) {
    String[] parsed = s.split(" ");
    submitTime = Integer.parseInt(parsed[1]);
    ID = Integer.parseInt(parsed[2]);
    estRuntime = Integer.parseInt(parsed[3]);
    core = Integer.parseInt(parsed[4]);
    memory = Integer.parseInt(parsed[5]);
    disk = Integer.parseInt(parsed[6]);
  }

  @Override
  public String toString() {
    return "JOBN " + ID;
  }

  public int getSubmitTime() {
    return this.submitTime;
  }

  public int getID() {
    return this.ID;
  }

  public int getEstRuntime() {
    return this.estRuntime;
  }

  public int getCore() {
    return this.core;
  }

  public int getMemory() {
    return this.memory;
  }

  public int getDisk() {
    return this.disk;
  }
}
