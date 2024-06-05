package org.main;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Structure of Ro usage records
 */
@AllArgsConstructor
@Getter
@Setter
public class Record {

    /**
     * MSISDN is the main identifier of the record
     */
    String MSISDN;
    /**
     * Data usage (in units of input data)
     */
    Long dataUsage;
    /**
     * Voice usage (in units of input data)
     */
    Long voiceUsage;
    /**
     * SMS usage (in units of input data)
     */
    Long smsUsage;
    /**
     * Cash usage (in units of input data)
     */
    Long cashUsage;
    /**
     * Monthly spent cash (in units of input data)
     */
    Long monthlyPurchases;
    /**
     * Delimiter to output data as string
     */
    String delimiter;

    /**
     * Constructor with default delimiter as comma (,)
     * @param cashUsage Cash usage (in units of input data)
     * @param smsUsage SMS usage (in units of input data)
     * @param voiceUsage Voice usage (in units of input data)
     * @param dataUsage Data usage (in units of input data)
     * @param MSISDN MSISDN is the main identifier of the record
     */
    public Record(Long cashUsage, Long smsUsage, Long voiceUsage,
                  Long dataUsage, Long monthlyPurchases, String MSISDN) {
        this.cashUsage = cashUsage;
        this.smsUsage = smsUsage;
        this.voiceUsage = voiceUsage;
        this.dataUsage = dataUsage;
        this.monthlyPurchases = monthlyPurchases;
        this.MSISDN = MSISDN;
        this.delimiter = ",";
    }

    /**
     * Set usage based on Record Type
     * @param RT Record Type
     * @param usage Usage in units of input data
     */
    public void setUsage(Integer RT, Long usage) {
        switch (RT) {
            case 1:
                setDataUsage(usage);
                break;
            case 2:
                setVoiceUsage(usage);
                break;
            case 3:
                setSmsUsage(usage);
                break;
            case 5:
                setCashUsage(usage);
                break;
        }
    }

    /**
     * Append usage based on Record Type
     * @param RT Record Type
     * @param usage Usage in units of input data
     */
    public void appendUsage(Integer RT, Long usage) {
        switch (RT) {
            case 1:
                this.dataUsage += usage;
                break;
            case 2:
                this.voiceUsage += usage;
                break;
            case 3:
                this.smsUsage += usage;
                break;
            case 5:
                this.cashUsage += usage;
                break;
        }
    }

    /**
     * Method to output data as a string in a required format for Machine Learning
     * @return String representing a single Ro Record Usage
     */
    @Override
    public String toString() {
        return  MSISDN + delimiter +
                voiceUsage + delimiter +
                smsUsage + delimiter +
                cashUsage + delimiter +
                monthlyPurchases;
    }

    /**
     * Equals based on MSISDN
     * @param o Another Ro Record object
     * @return True if records are equal or false if they are not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return Objects.equals(MSISDN, record.MSISDN);
    }

    /**
     * Hash code based on MSISDN
     * @return Ro Record object hash code
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(MSISDN);
    }

    /**
     * Checks if all record usages are equal to {@code 0}
     * @return {@code TRUE} if all usages are {@code 0}
     */
    public boolean isZeroUsage() {
        return voiceUsage == 0 &&
                smsUsage == 0 &&
                cashUsage == 0 &&
                dataUsage == 0 &&
                monthlyPurchases == 0;
    }
}