public class Server {
  String type, ID, state, curStartTime, core, mem, disk, wJobs, rJobs;

  public Server (String s) {
    String[] parsed = s.split(" ");
    System.out.println(parsed);
    type = parsed[0];
    ID = parsed[1];
    state = parsed[2];
    curStartTime = parsed[3];
    core = parsed[4];
    mem = parsed[5];
    disk = parsed[6];
    disk = parsed[7];
    // wJobs = parsed[8];
    // rJobs = parsed[9];
  }

  public Server () {

  }

}