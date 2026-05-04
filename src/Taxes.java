public enum Taxes {
    IL(0.06),
    CA(0.06),
    NY(0.06);

    private final double rate;

    Taxes(double rate) {
        this.rate = rate;
    }

    public double getRate() { return rate; }
}
