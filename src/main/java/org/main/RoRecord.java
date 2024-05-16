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
public class RoRecord {

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
    public RoRecord(Long cashUsage, Long smsUsage, Long voiceUsage, Long dataUsage, String MSISDN) {
        this.cashUsage = cashUsage;
        this.smsUsage = smsUsage;
        this.voiceUsage = voiceUsage;
        this.dataUsage = dataUsage;
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
     * Method to output data as a string in a required format for ML
     * @return String representing a single Ro Record Usage
     */
//    @Override
//    public String toString() {
//        return  MSISDN + delimiter +
//                dataUsage + delimiter +
//                voiceUsage + delimiter +
//                smsUsage + delimiter +
//                cashUsage;
//    }

    /**
     * Temporary toString for no cash no data trials
     * @return String representing a single Ro Record Usage
     */
    @Override
    public String toString() {
        return  MSISDN + delimiter +
                voiceUsage + delimiter +
                smsUsage;
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
        RoRecord roRecord = (RoRecord) o;
        return Objects.equals(MSISDN, roRecord.MSISDN);
    }

    /**
     * Hash code based on MSISDN
     * @return Ro Record object hash code
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(MSISDN);
    }
}
