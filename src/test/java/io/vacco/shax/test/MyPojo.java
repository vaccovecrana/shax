package io.vacco.shax.test;

import java.util.*;

public class MyPojo {

  public boolean moop;
  public boolean momo;

  public String zeep;

  public List<Integer> foos;
  public Map<String, Long> longs;
  public Set<String> cats;
  public String[] tags;

  private MyPojoNumbers numbers;
  public MyPojoNumbers noNumbers = null;

  public MyPojoNumbers getNumbers() { return numbers; }
  public void setNumbers(MyPojoNumbers numbers) { this.numbers = numbers; }

  public static MyPojo getInstance() {
    var longs = new LinkedHashMap<String, Long>();
    longs.put("One", 1L);
    longs.put("Two", 2L);
    longs.put("Three", 3L);

    var cats = new HashSet<String>();
    cats.add("fido");
    cats.add("garfield");
    cats.add("felix");

    var p = new MyPojo();
    p.foos = Arrays.asList(1, 2, 3, 4, 5);
    p.longs = longs;
    p.zeep = "zap";

    p.setNumbers(MyPojoNumbers.getInstance());
    p.cats = cats;

    p.tags = new String [] {"blog", "style", "living"};

    p.momo = true;
    p.moop = false;

    return p;
  }

}
