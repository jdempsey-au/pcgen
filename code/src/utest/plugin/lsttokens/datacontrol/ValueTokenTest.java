/*
 * Copyright (c) 2014 Tom Parker <thpr@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package plugin.lsttokens.datacontrol;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pcgen.cdom.content.UserFunction;
import pcgen.core.Campaign;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.CampaignSourceEntry;
import pcgen.rules.context.ConsolidatedListCommitStrategy;
import pcgen.rules.context.LoadContext;
import pcgen.rules.context.RuntimeLoadContext;
import pcgen.rules.context.RuntimeReferenceContext;
import plugin.lsttokens.testsupport.TokenRegistration;

public class ValueTokenTest extends TestCase
{

	static ValueToken token = new ValueToken();
	UserFunction function;

	protected LoadContext context;

	private static boolean classSetUpFired = false;
	protected static CampaignSourceEntry testCampaign;

	@BeforeClass
	public static final void classSetUp() throws URISyntaxException
	{
		testCampaign =
				new CampaignSourceEntry(new Campaign(), new URI(
					"file:/Test%20Case"));
		classSetUpFired = true;
	}

	@Override
	@Before
	public void setUp() throws PersistenceLayerException, URISyntaxException
	{
		if (!classSetUpFired)
		{
			classSetUp();
		}
		TokenRegistration.clearTokens();
		TokenRegistration.register(token);
		resetContext();
	}

	protected void resetContext()
	{
		URI testURI = testCampaign.getURI();
		context =
				new RuntimeLoadContext(new RuntimeReferenceContext(),
					new ConsolidatedListCommitStrategy());
		context.setSourceURI(testURI);
		context.setExtractURI(testURI);
		function = new UserFunction();
		function.setName("MyFunction");
	}

	@Test
	public void testInvalidInputNullString() throws PersistenceLayerException
	{
		assertFalse(token.parseToken(context, function, null).passed());
	}

	@Test
	public void testInvalidInputEmptyString() throws PersistenceLayerException
	{
		try
		{
			assertFalse(token.parseToken(context, function, "").passed());
		}
		catch (IllegalArgumentException e)
		{
			//This is ok too
		}
	}

	@Test
	public void testInvalidFormula() throws PersistenceLayerException
	{
		try
		{
			assertFalse(token.parseToken(context, function, "2+").passed());
		}
		catch (IllegalArgumentException e)
		{
			//This is ok too
		}
	}

	@Test
	public void testInvalidNonMatchingDefine() throws PersistenceLayerException
	{
		assertTrue(token.parseToken(context, function, "3+4").passed());
		try
		{
			assertFalse(token.parseToken(context, function, "2+3").passed());
		}
		catch (IllegalArgumentException e)
		{
			//This is ok too
		}
	}

	@Test
	public void testInvalidAllowMatchingDefine() throws PersistenceLayerException
	{
		assertTrue(token.parseToken(context, function, "3+4").passed());
		assertTrue(token.parseToken(context, function, "3+4").passed());
	}

	@Test
	public void testValidStringString() throws PersistenceLayerException
	{
		assertTrue(token.parseToken(context, function, "2+3").passed());
		String[] unparsed = token.unparse(context, function);
		assertNotNull(unparsed);
		assertEquals(1, unparsed.length);
		assertEquals("2+3", unparsed[0]);
	}

	@Test
	public void testValidStringNo() throws PersistenceLayerException
	{
		assertTrue(token.parseToken(context, function, "3-4").passed());
		String[] unparsed = token.unparse(context, function);
		assertNotNull(unparsed);
		assertEquals(1, unparsed.length);
		assertEquals("3-4", unparsed[0]);
	}

}
