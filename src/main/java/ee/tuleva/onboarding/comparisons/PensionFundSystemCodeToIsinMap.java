package ee.tuleva.onboarding.comparisons;

import java.util.HashMap;
import java.util.Map;

public class PensionFundSystemCodeToIsinMap {

    private static final Map<Integer, String> map;

    static {
        map = new HashMap<>();
        map.put(44, "EE3600019790"); //Pension Fund LHV 25
        map.put(35, "EE3600019808"); //Pension Fund LHV 50
        map.put(73, "EE3600109401"); //Pension Fund LHV Index
        map.put(45, "EE3600019816"); //Pension Fund LHV Interest
        map.put(47, "EE3600019832"); //Pension Fund LHV L
        map.put(39, "EE3600019774"); //Pension Fund LHV M
        map.put(46, "EE3600019824"); //Pension Fund LHV S
        map.put(38, "EE3600019766"); //Pension Fund LHV XL
        map.put(59, "EE3600019782"); //Pension Fund LHV XS
        map.put(48, "EE3600098430"); //Nordea Pension Fund A
        map.put(57, "EE3600103503"); //Nordea Pension Fund A Plus
        map.put(49, "EE3600098448"); //Nordea Pension Fund B
        map.put(50, "EE3600098455"); //Nordea Pension Fund C
        map.put(56, "EE3600103297"); //SEB Energetic Pension Fund
        map.put(75, "EE3600109427"); //SEB Energetic Pension Fund Index
        map.put(60, "EE3600019717"); //SEB Conservative Pension Fund
        map.put(51, "EE3600098612"); //SEB Optimal Pension Fund
        map.put(61, "EE3600019725"); //SEB Progressive Pension Fund
        map.put(58, "EE3600019733"); //Swedbank Pension Fund K1 (Conservative Strategy)
        map.put(36, "EE3600019741"); //Swedbank Pension Fund K2 (Balanced Strategy)
        map.put(37, "EE3600019758"); //Swedbank Pension Fund K3 (Growth Strategy)
        map.put(52, "EE3600103248"); //Swedbank Pension Fund K4 (Equity Strategy)
        map.put(74, "EE3600109393"); //Swedbank Pension Fund K90-99 (Life-cycle Strategy)

    }

    static String getIsin(int code){
        return map.get(code);
    }

}
