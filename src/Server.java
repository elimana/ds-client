public class Server implements Comparable<Server>{
  private String type, state;
  private int ID, curStartTime, core, mem, disk, bootTime;
  private float hourlyRate;

  public Server (String s) {
    String[] parsed = s.split(" ");
    type = parsed[0];
    ID = Integer.parseInt(parsed[1]);
    state = parsed[2];
    curStartTime = Integer.parseInt(parsed[3]);
    core = Integer.parseInt(parsed[4]);
    mem = Integer.parseInt(parsed[5]);
    disk = Integer.parseInt(parsed[6]);
    bootTime = Integer.parseInt(parsed[7]);
    hourlyRate = Float.parseFloat(parsed[8]);
  }

  public String getType() {
    return this.type;
  }

  public int getID() {
    return this.ID;
  }

  public String getState() {
    return this.state;
  }

  public int getCurStartTime() {
    return this.curStartTime;
  }

  public int getCore() {
    return this.core;
  }

  public int getMem() {
    return this.mem;
  }

  public int getDisk() {
    return this.disk;
  }

  public int getBootTime() {
    return this.bootTime;
  }

  public float getHourlyRate() {
    return this.hourlyRate;
  }

  @Override
  public String toString () {
    return type + " " + ID;
  }

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
   * First priority to sort cores descending.
   * Second priority to sort ID ascending.
   * 
   * @param other server to compare to
   * @return int
   */
  @Override
  public int compareTo(Server other) {
    if (this.core < other.core) {
      return 1;
    }
    else if (this.core > other.core) {
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
