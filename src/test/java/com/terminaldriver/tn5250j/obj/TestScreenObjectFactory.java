package com.terminaldriver.tn5250j.obj;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.terminaldriver.tn5250j.TerminalDriver;
import com.terminaldriver.tn5250j.annotation.FindBy;
import com.terminaldriver.tn5250j.annotation.ScreenAttribute;
import com.terminaldriver.tn5250j.mock.MockScreenUtil;
import com.terminaldriver.tn5250j.util.ScreenFieldReader;
import com.terminaldriver.tn5250j.util.ScreenUtils;


public class TestScreenObjectFactory {
	TerminalDriver driver;
	int currentPosition;
	@Rule public TestName testName = new TestName();
	FindBy findBy;
	private ScreenFieldReader reader;
	
	@Before
	public void setUp() throws Exception{
		final InputStream data = getClass().getResourceAsStream("/com/terminaldriver/tn5250j/util/wrkmbrpdm.json");
		driver = MockScreenUtil.createTestDriver(data);
		findBy = getClass().getMethod(testName.getMethodName()).getAnnotation(FindBy.class);
		reader = new ScreenFieldReader(driver);
	}
	
	@Test
	@FindBy(text="Work with Members Using PDM",row=1,attribute=ScreenAttribute.WHT)
	public void testFindByColorTextAndRow() throws Exception {
		ScreenTextBlock screenField = ScreenUtils.applyFindScreenTextBlock(driver,findBy,reader,currentPosition);
		assertNotNull(screenField);
		assertEquals(1,screenField.startRow());
		assertEquals("                Work with Members Using PDM             ",screenField.value);
	}
	
	@Test
	@FindBy(text="Work with Members Using PDM",row=2,attribute=ScreenAttribute.WHT)
	public void testFindByColorTextAndRow2() throws Exception {
		ScreenTextBlock screenField = ScreenUtils.applyFindScreenTextBlock(driver,findBy,reader, currentPosition);
		assertNull(screenField);
	}
	
	@FindBy(attribute=ScreenAttribute.WHT)
	Object colorWhiteFinder;
	
	@Test
	@FindBy(row=10,attribute=ScreenAttribute.WHT)
	public void testFindByColorAndRow() throws Exception {
		final ScreenTextBlock screenField = ScreenUtils.applyFindScreenTextBlock(driver,findBy,reader, currentPosition);
		assertNotNull(screenField);
		assertEquals(10,screenField.startRow());
		assertEquals("Opt  Member      Type        Text                                             ",screenField.value);
		
		final FindBy findBy2 = getFinder("colorWhiteFinder");
		ScreenTextBlock screenField2 = ScreenUtils.applyFindScreenTextBlock(driver,findBy2,reader, screenField.endPos()+1);
		assertNotNull(screenField2);
		assertEquals(19,screenField2.startRow());
		assertEquals("      Bottom",screenField2.value);
		
		ScreenTextBlock screenField3 = ScreenUtils.applyFindScreenTextBlock(driver,findBy2,reader, screenField2.endPos()+1);
		assertNotNull(screenField3);
		assertEquals(24,screenField3.startRow());
		assertEquals("                                         (C) COPYRIGHT IBM CORP. 1981, 2003.",screenField3.value);
		
		ScreenTextBlock screenField4 = ScreenUtils.applyFindScreenTextBlock(driver,findBy2,reader, screenField3.endPos()+1);
		assertNotNull(screenField4);
	}
		
	public static FindBy getFinder(String name) throws NoSuchFieldException, SecurityException{
		return TestScreenObjectFactory.class.getDeclaredField(name).getAnnotation(FindBy.class);
	}

}
