/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package openwar.DB;

import java.util.HashMap;
import openwar.DB.GenericBuilding.GenericRecruitmentStats;

/**
 *
 * @author kehl
 */
public class Building {

    public class RecruitmentStats {

        public String refName;
        public int currUnits, turnsTillNextUnit;
        
        public RecruitmentStats(){}
    }
    public HashMap<String, RecruitmentStats> recStats;
    public String refName;
    public int level;

    public Building() {
                recStats = new HashMap<String, RecruitmentStats>();

    }

    public Building(String ref, int l) {
        refName = ref;
        level = l;
        recStats = new HashMap<String, RecruitmentStats>();

    }
    
    public void createRecruitmentStats(GenericRecruitmentStats grs)
    {
        RecruitmentStats rs = new RecruitmentStats();
        rs.refName = grs.refName;
        rs.currUnits = grs.maxUnits;
        rs.turnsTillNextUnit = grs.turnsPerUnit;
        recStats.put(refName, rs);
    }
}
