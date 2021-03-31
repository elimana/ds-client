public class Server implements Comparable<Server>{
  String type, ID, state, curStartTime, core, mem, disk, wJobs, rJobs;

  public Server (String s) {
    String[] parsed = s.split(" ");
    
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

  @Override
  public String toString () {
    return type + " " + ID;
  }

  @Override
  public int compareTo(Server other) {
    
    if (Integer.parseInt(this.core) > Integer.parseInt(other.core)) {
       
       return 1;
       
    } else if (Integer.parseInt(this.core) < Integer.parseInt(other.core)) {
       
       return -1;
       
    } else {
       
       return 0;
       
    }
       
  }

}
