package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.DandelionOwner;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.DandelionOwner.Dummy_Core;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.LinkedList;

public class Core_Calling extends Buff {
    {
        revivePersists = true;
    }
    private LinkedList<Dummy_Core> cores = new LinkedList<>();
    private final LinkedList<Integer> callingTimes = new LinkedList<>();
    public void addCore(Dummy_Core core){
        addCore(core, 0);
    }
    public void addCore(Dummy_Core core, float delay) {
        cores.add(core);
        callingTimes.add(5 + (int) delay);
    }
    @Override
    public boolean act(){
        spend(TICK);
        int size = Math.min(cores.size(), callingTimes.size());
        while (cores.size() > size)
            cores.removeLast();
        while (callingTimes.size() > size)
            callingTimes.removeLast();

        for (int i = 0; i < size; i++)
            if (callingTimes.get(i) > 0)
                callingTimes.set(i, callingTimes.get(i) - 1);

        ArrayList<Integer> placeable = new ArrayList<>();
        for (int i = 0; i < PathFinder.NEIGHBOURS25.length; i++) {
            int p = target.pos + PathFinder.NEIGHBOURS25[i];
            if (Actor.findChar(p) == null && Dungeon.level.passable[p])
                placeable.add(p);
        }

        while (!placeable.isEmpty() && !callingTimes.isEmpty() && callingTimes.get(0) == 0){
            int pos = placeable.remove(Random.Int(placeable.size()));
            callingTimes.removeFirst();
            cores.removeFirst().summon(pos);
        }
        if (cores.isEmpty())
            detach();

        return true;
    }
    private static final String CALLING_CORES = "CALLING_CORES";
    private static final String CALLING_TIMES = "CALLING_TIMES";
    @Override
    public void storeInBundle( Bundle bundle ){
        super.storeInBundle(bundle);
        bundle.put(CALLING_CORES, cores);
        int[] times = new int[callingTimes.size()];
        for (int i = 0; i < callingTimes.size(); i++)
            times[i] = callingTimes.get(i);
        bundle.put(CALLING_TIMES, times);
    }
    @Override
    public void restoreFromBundle( Bundle bundle ){
        super.restoreFromBundle(bundle);
        cores = bundle.getLinkedList(CALLING_CORES, Dummy_Core.class);
        int[] times = bundle.getIntArray(CALLING_TIMES);
        callingTimes.clear();
        for (int time : times)
            callingTimes.add(time);
    }
}
