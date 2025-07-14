package com.github.akruk.varia;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.random.RandomGenerator;



public class test {

    enum letter {
        a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,r,s,t,u,v,w,y,z;
    }


    private static boolean[] enumArray(letter... truevalues) {
        var array = new boolean[letter.values().length];
        for (var v : truevalues) {
            array[v.ordinal()] = true;
        }
        return array;
    }

    private static final boolean[] resultarray = enumArray(letter.d, letter.f, letter.g, letter.h);
    public static boolean testArray(letter x) {
        return resultarray[x.ordinal()];
    }

    public static boolean testSwitchValue(letter x) {
        return switch (x) {
            case a -> false;
            case b -> false;
            case c -> false;
            case d -> true;
            case e -> false;
            case f -> true;
            case g -> true;
            case h -> true;
            case i -> false;
            case j -> false;
            case k -> false;
            case l -> false;
            case m -> false;
            case n -> false;
            case o -> false;
            case p -> false;
            case r -> false;
            case s -> false;
            case t -> false;
            case u -> false;
            case v -> false;
            case w -> false;
            case y -> false;
            case z -> false;
            default -> false;
        };
    }

    public static boolean testSwitchInstruction(letter x) {
        switch (x) {
            case a:
                return false;
            case b:
                return false;
            case c:
                return false;
            case d:
                return true;
            case e:
                return false;
            case f:
                return true;
            case g:
                return true;
            case h:
                return true;
            case i:
                return false;
            case j:
                return false;
            case k:
                return false;
            case l:
                return false;
            case m:
                return false;
            case n:
                return false;
            case o:
                return false;
            case p:
                return false;
            case r:
                return false;
            case s:
                return false;
            case t:
                return false;
            case u:
                return false;
            case v:
                return false;
            case w:
                return false;
            case y:
                return false;
            case z:
                return false;
            default:
                return false;
        }

    }


    public static interface x {
        boolean isTrue();
    }
    public static class p {
        boolean isTrue() {
            return true;
        }
    }

    public static void main(String[] args) {
        final int lettercount = letter.values().length;
        final letter[] integers = Random.from(RandomGenerator.getDefault())
                .ints(100000)
                .mapToObj(i -> letter.values()[Math.abs(i % lettercount)])
                .toArray(letter[]::new);
        final int numberoftries = Integer.parseInt(args[0]);
        long tries = 0;
        long sumArray = 0;
        long sumSwitchVal = 0;
        long sumSwitchInstruction = 0;
        while (tries < numberoftries) {
            var one = Instant.now();
            for (letter i : integers) {
                testArray(i);
            }
            var oneEnd = Instant.now();
            for (letter i : integers) {
                testSwitchValue(i);
            }
            var two = Instant.now();
            for (letter i : integers) {
                testSwitchInstruction(i);
            }
            var twoEnd = Instant.now();
            sumArray += ChronoUnit.NANOS.between(one, oneEnd);
            sumSwitchVal += ChronoUnit.NANOS.between(oneEnd, two);
            sumSwitchInstruction += ChronoUnit.NANOS.between(two, twoEnd);
            tries++;
        }
        long avgArray = sumArray/numberoftries;
        long avgSwitchVal = sumSwitchVal/numberoftries;
        long avgSwitchInstruction = sumSwitchInstruction/numberoftries;
        System.out.println(avgArray);
        System.out.println(avgSwitchVal);
        System.out.println(avgSwitchInstruction);
    }

}
