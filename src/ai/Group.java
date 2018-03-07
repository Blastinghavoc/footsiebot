package footsiebot.ai;

import java.util.ArrayList;
import java.lang.Comparable;

public class Group implements Comparable<Group> {
  private ArrayList<Company> companies;
  private String code;
  private Float groupCount;
  private Float adjustment = 0.0f;

/**
 * Initializes group with just the code
 * @param  String code          [description]
 * @return        [description]
 */
  public Group(String code) {
    this.code = code;
  }
  /**
   * Initializes a group with its data
   * @param  ArrayList<Company> companies     list of companies that make up the group
   * @param  String             code          the unique identifier
   * @param  Float              groupCount    counter for how many times group has been querieds
   * @param  Float              adjustment
   * @return                    group object instance
   */
  public Group(ArrayList<Company> companies, String code, Float groupCount, Float adjustment) {
    this.companies = companies;
    this.code = code;
    this.groupCount = groupCount;
    this.adjustment = adjustment;

  }
  /**
   * Add the companies
   * @param ArrayList<Company> companies list of companies to add
   */
  public void addCompanies(ArrayList<Company> companies) {
    this.companies = companies;
  }
  /**
   * Gets all the companies in this group
   * @return list of companies
   */
  public ArrayList<Company> getCompanies() {
    return companies;
  }
  /**
   * Gets the identifier for this group
   * @return a string for the identifier
   */
  public String getGroupCode() {
	   return code;
  }
  /**
   * Set the counter
   * @param Float groupCount
   */
  public void setGroupCount(Float groupCount) {
    this.groupCount = groupCount;
  }
  /**
   * Set the adjustment
   * @param Float adjustments
   */
  public void setAdjustment(Float adjustments) {
    this.adjustment = adjustment;
  }
  /**
   * Get priority as the counter minus adjustment
   * @return the priority
   */
  public Float getGroupPriority() {
    return getGroupCount() - adjustment;
  }
  /**
   * Gets how many times this group has been queried
   * @return
   */
  public Float getGroupCount() {
    return groupCount;
  }
  /**
   * Gets the group adjustment
   * @return
   */
  public Float getAdjustment() {
    return adjustment;
  }
  /**
   * To sort the groups by priority
   * @param  Group g
   * @return       
   */
  @Override
  public int compareTo(Group g) {
    Float r =  g.getGroupPriority() - this.getGroupPriority();

    if(r < 0) {
      return -1;
    } else if (r == 0) {
      return 0;
    } else {
      return 1;
    }
  }



}
