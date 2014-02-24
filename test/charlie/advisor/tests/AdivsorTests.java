/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author Narsil
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    Test00_1217_26.class, Test01_1217_7A.class,
    Test02_511_26.class, Test03_511_7A.class,
    Test04_A210_26.class, Test05_A210_7A.class,
    Test06_22AA_26.class, Test07_22AA_7A.class
})

public class AdivsorTests {

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
