package footsiebot.datagathering;

/**
 * Used to store current FTSE 100 data.
 */
public class ScrapeResult {
    private String[] codes;
    private String[] names;
    private String[] groups;
    private Float[] prices;
    private Float[] absChange;
    private Float[] percChange;
    private Integer[] tradeVolume;

    /**
     * Constructor that takes in arrays occupied with the relevant data fields.
     * @return An occupied ScrapeResult.
     */
    public ScrapeResult(String[] codes, String[] names, String[] groups, Float[] prices, Float[] absChange, Float[] percChange, Integer[] tradeVolume) {
        this.codes = codes;
        this.names = names;
        this.groups = groups;
        this.prices = prices;
        this.absChange = absChange;
        this.percChange = percChange;
        this.tradeVolume = tradeVolume;
    }

    /**
     * Fetches a company code at a specific index.
     * @param index the index of the company.
     * @return      the company's code.
     */
    public String getCode(int index) {
        return codes[index];
    }

    /**
     * Fetches a company name at a specific index.
     * @param index the index of the company.
     * @return      the company's name.
     */
    public String getName(int index) {
        return names[index];
    }

    /**
     * Fetches a company group name at a specific index.
     * @param index the index of the company.
     * @return      the company's group name.
     */
    public String getGroup(int index) {
        return groups[index];
    }

    /**
     * Fetches a company's spot price at a specific index.
     * @param index the index of the company.
     * @return      the company's spot price.
     */
    public Float getPrice(int index) {
        return prices[index];
    }

    /**
     * Fetches a company's absolute change at a specific index.
     * @param index the index of the company.
     * @return      the company's absolute change.
     */
    public Float getAbsChange(int index) {
        return absChange[index];
    }

    /**
     * Fetches a company's percentage change at a specific index.
     * @param index the index of the company.
     * @return      the company's percentage change.
     */
    public Float getPercChange(int index) {
        return percChange[index];
    }

    /**
     * Fetches a company's absolute change at a specific index.
     * @param index the index of the company.
     * @return      the company's absolute change.
     */
    public Integer getVolume(int index) {
        return tradeVolume[index];
    }

    /**
     * Fetches the number of companies stored in the ScrapeResult.
     * @return the number of companies within the ScrapeResult.
     */
    public int getSize() {
        return names.length;
    }

    /**
     * Fetches the number of companies stored in the ScrapeResult.
     * @param sr the ScrapeResult for which equality will be tested.
     * @return   True if sr is storing identical data to this ScrapeResult,
     *           False otherwise.
     */
    public Boolean equals(ScrapeResult sr){
        Boolean areEqual = true;
        if (!this.codes.equals(sr.codes)) areEqual = false;
        if (!this.names.equals(sr.names)) areEqual = false;
        if (!this.groups.equals(sr.groups)) areEqual = false;
        if (!this.prices.equals(sr.prices)) areEqual = false;
        if (!this.absChange.equals(sr.absChange)) areEqual = false;
        if (!this.percChange.equals(sr.percChange)) areEqual = false;
        return areEqual;
    }
}
