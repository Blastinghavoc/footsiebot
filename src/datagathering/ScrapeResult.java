package footsiebot.datagathering;


public class ScrapeResult {
    private String[] codes;
    private String[] names;
    private String[] groups;
    private Float[] prices;
    private Float[] absChange;
    private Float[] percChange;

    public ScrapeResult(String[] codes, String[] names, String[] groups, Float[] prices, Float[] absChange, Float[] percChange) {
        this.codes = codes;
        this.names = names;
        this.groups = groups;
        this.prices = prices;
        this.absChange = absChange;
        this.percChange = percChange;
    }

    public String getCode(int index) {
        return codes[index];
    }

    public String getName(int index) {
        return names[index];
    }

    public String getGroup(int index) {
        return groups[index];
    }

    public Float getPrice(int index) {
        return prices[index];
    }

    public Float getAbsChange(int index) {
        return absChange[index];
    }

    public Float getPercChange(int index) {
        return percChange[index];
    }

}
