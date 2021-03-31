public class Job {
  String submitTime, ID, estRuntime, core, memory, disk;
  
  public Job(String s) {
    String[] parsed = s.split(" ");

    submitTime = parsed[1];
    ID = parsed[2];
    estRuntime = parsed[3];
    core = parsed[4];
    memory = parsed[5];
    disk = parsed[6];
  }

  public Job() {
    
  }

  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return "JOBN " + ID;
  }
}
