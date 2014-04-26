/*
 * Contains the test suite for the IAdvisor CharliePlugin.
 */
package charlie.advisor.tests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import charlie.bs.section1.*;
import charlie.bs.section2.*;
import charlie.bs.section3.*;
import charlie.bs.section4.*;

/**
 * @author Joe
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    Test00_12_2.class, Test01_12_7.class,
    Test02_5_2.class, Test03_5_7.class,
    Test04_A2_2.class, Test05_A2_7.class,
    Test06_22_2.class, Test07_22_7.class
})

public class AdvisorTests {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
