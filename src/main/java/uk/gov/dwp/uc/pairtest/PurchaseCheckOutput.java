package uk.gov.dwp.uc.pairtest;

public class PurchaseCheckOutput {
    private final boolean isInvalid;
    private final String errorMessage;

    public PurchaseCheckOutput(boolean isInvalid, String errorMessage) {
        this.isInvalid = isInvalid;
        this.errorMessage = errorMessage;
    }

    public boolean getIsInvalid() {
        return isInvalid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
