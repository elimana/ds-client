import java.util.*;

/**
 * Constructs and provides interfaces for the server object
 */
public class Server implements Comparable<Server>{
  private String type, state;
  private int ID, curStartTime, core, mem, disk, bootTime, wJobs, rJobs;
  private float hourlyRate;
  private List<Job> serverJobs;

  /**
   * Constructs a server object from a string
   * 
   * @param s Constructor input string with server fields delimited by the space
   *          character
   */
  public Server (String s) {
    String[] parsed = s.split(" ");
    type = parsed[0];
    ID = Integer.parseInt(parsed[1]);
    state = parsed[2];
    curStartTime = Integer.parseInt(parsed[3]);
    core = Integer.parseInt(parsed[4]);
    mem = Integer.parseInt(parsed[5]);
    disk = Integer.parseInt(parsed[6]);
    wJobs = Integer.parseInt(parsed[7]);
    rJobs = Integer.parseInt(parsed[8]);
  }

  public Server (String type, int ID, String state, int curStartTime, int core, int mem, int disk, int bootTime, float hourlyRate) {
    this.type = type;
    this.state = state;
    this.ID = ID;
    this.curStartTime = curStartTime;
    this.core = core;
    this.mem = mem;
    this.disk = disk;
    this.bootTime = bootTime;
    this.wJobs = 0;
    this.rJobs = 0;
    this.hourlyRate = hourlyRate;
  }

  public Server (String type, int ID) {
    this.type = type;
    this.ID = ID;
    this.state = "unknown";
    this.curStartTime = -1;
    this.core = -1;
    this.mem = -1;
    this.disk = -1;
    this.bootTime = -1;
    this.wJobs = 0;
    this.rJobs = 0;
    this.hourlyRate = -1;
  }

  /**
   * Getter function for server type
   * 
   * @return server type in string format
   */
  public String getType() {
    return this.type;
  }

  /**
   * Getter function for server ID
   * 
   * @return server ID in int format
   */
  public int getID() {
    return this.ID;
  }

  /**
   * Getter function for server state
   * 
   * @return server state in string format
   */
  public String getState() {
    return this.state;
  }

  /**
   * Getter function for current start time
   * 
   * @return current start time in int format
   */
  public int getCurStartTime() {
    return this.curStartTime;
  }

  /**
   * Getter function for server CPU core count
   * 
   * @return Number of server CPU cores in int format
   */
  public int getCore() {
    return this.core;
  }

  /**
   * Getter function for server memory
   * 
   * @return server memory (MB) in int format
   */
  public int getMem() {
    return this.mem;
  }

  /**
   * Getter function for server disk space
   * 
   * @return server disk space (MB) in int format
   */
  public int getDisk() {
    return this.disk;
  }

  /**
   * Getter function for server boot time
   * 
   * @return server boot time in int format
   */
  public int getBootTime() {
    return this.bootTime;
  }

  /**
   * Getter function for server hourly rate
   * 
   * @return server hourly rate in floating point format
   */
  public float getHourlyRate() {
    return this.hourlyRate;
  }

  public int getWJobs() {
    return this.wJobs;
  }

  public int getRJobs() {
    return this.rJobs;
  }



  public List<Job> getServerJobs() {
    return this.serverJobs;
  }

  public void setServerJobs(List<Job> serverJobs) {
    this.serverJobs = serverJobs;
  }

  /**
   * Function for displaying server ID and type together, in a friendly format.
   * Useful for testing.
   * 
   * @return Server type followed by server ID, separated by a space character
   */
  @Override
  public String toString () {
    return type + " " + ID;
  }

  /**
   * Function for displaying all server attributes together, in a detailed form.
   * Attributes are separated by a space character. Useful for testing.
   * 
   * @return String containing all server details, separated by space characters
   */
  public String getAllServerDetails() {

    String serverDetails = "";

    serverDetails += (this.type + " ");
    serverDetails += (Integer.toString(this.ID) + " ");
    serverDetails += (this.state + " ");
    serverDetails += (Integer.toString(this.curStartTime) + " ");
    serverDetails += (Integer.toString(this.core) + " ");
    serverDetails += (Integer.toString(this.mem) + " ");
    serverDetails += (Integer.toString(this.disk) + " ");
    serverDetails += (Integer.toString(this.bootTime) + " ");
    serverDetails += Float.toString(this.hourlyRate);

    return serverDetails;

  }

  /**
   * First priority to sort cores ascending. Second priority to sort ID
   * ascending.
   * 
   * @param other server object to compare against
   * @return int varying according to results of comparison (1 or 0 or -1)
   */
  @Override
  public int compareTo(Server other) {
    if (this.core > other.core) {
      return 1;
    }
    else if (this.core < other.core) {
      return -1;
    }
    else {
      if(this.ID > other.ID) {
        return 1;
      }
      if(this.ID < other.ID) {
        return -1;
      }
      return 0;
    }
  }
}
