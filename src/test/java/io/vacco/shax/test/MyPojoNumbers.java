package io.vacco.shax.test;

public class MyPojoNumbers {

  public int foo;
  public double bar;
  public long baz;
  public float meep;

  public static MyPojoNumbers getInstance() {
    var numbers = new MyPojoNumbers();
    numbers.foo = 42;
    numbers.bar = 1.34;
    numbers.baz = 23L;
    numbers.meep = 2.0f;
    return numbers;
  }

}
