public class Server implements Comparable<Server>{
  private String type, state;
  private int ID, curStartTime, core, mem, disk;

  public Server (String s) {
    String[] parsed = s.split(" ");
    type = parsed[0];
    ID = Integer.parseInt(parsed[1]);
    state = parsed[2];
    curStartTime = Integer.parseInt(parsed[3]);
    core = Integer.parseInt(parsed[4]);
    mem = Integer.parseInt(parsed[5]);
    disk = Integer.parseInt(parsed[6]);
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

  @Override
  public String toString () {
    return type + " " + ID;
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
