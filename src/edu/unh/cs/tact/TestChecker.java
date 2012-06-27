// Copyright © 2012 Steve McCoy under the MIT license.
package edu.unh.cs.tact;

import org.junit.*;
import static org.junit.Assert.*;

public class TestChecker{
	@Test public void skipNull(){
		Checker.check(null);
	}
}
