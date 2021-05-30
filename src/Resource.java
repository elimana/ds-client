/**
 * This class represents the available server resources.
 */
public class Resource {
  private int availableCores, availableMem, availableDisk, pendingJobs;

  public Resource(int availableCores, int availableMem, int availableDisk, int pendingJobs) {
    this.availableCores = availableCores;
    this.availableMem = availableMem;
    this.availableDisk = availableDisk;
    this.pendingJobs = pendingJobs;
  }

  public int getAvailableCores() {
    return this.availableCores;
  }

  public int getAvailableMem() {
    return this.availableMem;
  }

  public int getAvailableDisk() {
    return this.availableDisk;
  }

  public int getPendingJobs() {
    return this.pendingJobs;
  }

  @Override
  public String toString() {
    return "{" +
      " availableCores='" + getAvailableCores() + "'" +
      ", availableMem='" + getAvailableMem() + "'" +
      ", availableDisk='" + getAvailableDisk() + "'" +
      "}";
  }
}
