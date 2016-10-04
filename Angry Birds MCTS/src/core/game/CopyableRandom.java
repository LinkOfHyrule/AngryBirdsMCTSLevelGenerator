package core.game;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

// http://stackoverflow.com/questions/18493319/copy-instance-variable-of-type-java-util-random-to-create-object-in-same-state/18531276#18531276
public class CopyableRandom extends Random implements Copyable<CopyableRandom>
{
  private final AtomicLong seed = new AtomicLong(0L);

  private final static long multiplier = 0x5DEECE66DL;
  private final static long addend = 0xBL;
  private final static long mask = (1L << 48) - 1;

  public CopyableRandom() { this(++seedUniquifier + System.nanoTime()); }
  private static volatile long seedUniquifier = 8682522807148012L;

  public CopyableRandom(long seed) { this.seed.set((seed ^ multiplier) & mask); }

  /* copy of superclasses code, as you can seed the seed changes */
  @Override
  protected int next(int bits)
  {
    long oldseed, nextseed;
    AtomicLong seed_ = this.seed;
    do
    {
      oldseed = seed_.get();
      nextseed = (oldseed * multiplier + addend) & mask;
    } while (!seed_.compareAndSet(oldseed, nextseed));
    return (int) (nextseed >>> (48 - bits));
  }

  /* necessary to prevent changes to seed that are made in constructor */
  @Override
  public CopyableRandom copy() { return new CopyableRandom((seed.get() ^ multiplier) & mask); }

}