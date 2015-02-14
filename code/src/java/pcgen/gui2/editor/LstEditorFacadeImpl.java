/*
 * LstEditorFacadeImpl.java
 * Copyright 2013 (C) James Dempsey <jdempsey@users.sourceforge.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on 30/12/2013
 *
 * $Id: LstEditorFacadeImpl.java 22711 2013-12-31 00:15:07Z jdempsey $
 */
package pcgen.gui2.editor;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pcgen.cdom.base.CDOMObject;
import pcgen.core.Globals;
import pcgen.core.PObject;
import pcgen.facade.util.DefaultListFacade;
import pcgen.facade.util.ListFacade;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.TokenLibrary;
import pcgen.rules.persistence.token.CDOMToken;
import pcgen.util.Logging;

/**
 * This is an implementation of the main facade for editing a specific rules 
 * object. It tracks the current state of the rules object and allows tags to 
 * be added, edited and removed.  
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision: 22711 $
 */
public class LstEditorFacadeImpl implements LstEditorFacade, ActionListener
{

	private final CDOMObject rulesObject;
	private final DefaultListFacade<String> tokens;
	private final DefaultListFacade<String> objectTags;

	/**
	 * Create a new instance of LstEditorFacadeImpl for a particular rules 
	 * object.
	 * @param rulesObject The target object which is to be edited.
	 */
	public LstEditorFacadeImpl(CDOMObject rulesObject)
	{
		this.rulesObject = rulesObject;
		this.tokens = new DefaultListFacade<String>();
		this.objectTags = new DefaultListFacade<String>();
		
		buildTokenList();
		buildTagList();
	}

	/**
	 * Create a list of tokens which are valid for the current type of rules 
	 * object. Tags specific to the type will be ordered first followed by 
	 * global tags. 
	 */
	@SuppressWarnings("rawtypes")
	private void buildTokenList()
	{
		Class tokenClasses[] = new Class[]{rulesObject.getClass(), PObject.class, CDOMObject.class};
		boolean needSeparatorNext = false;
		for (Class tgtClass : tokenClasses)
		{
			List<String> tokenList = new ArrayList<String>();
			@SuppressWarnings("unchecked")
			Set<CDOMToken<?>> tokenSet = TokenLibrary.getTokensForCLass(tgtClass);
			for (CDOMToken<?> tok : tokenSet)
			{
				tokenList.add(tok.getTokenName());
			}
			Collections.sort(tokenList);
			if (needSeparatorNext && !tokenList.isEmpty())
			{
				tokens.addElement("--------------");
			}
			for (String tokenName : tokenList)
			{
				tokens.addElement(tokenName);
			}
			needSeparatorNext = true;
		}
	}
	
	/**
	 * Create a list of tags already associated with the rules object.
	 */
	private void buildTagList()
	{
		LoadContext context = Globals.getContext();
		List<String> tagList = new ArrayList<String>();
		Collection<String> result = context.unparse(rulesObject);
		for (String tag : result)
		{
			tagList.add(tag);
		}
		objectTags.updateContents(tagList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListFacade<String> getValidTokens()
	{
		return tokens;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ListFacade<String> getObjectTags()
	{
		return objectTags;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName()
	{
		return rulesObject.getDisplayName();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends CDOMObject> TagEditor<T> getEditorForToken(String tokenName)
	{
		LoadContext context = Globals.getContext();
		List<? extends CDOMToken<T>> tokenList = (List<? extends CDOMToken<T>>) context.getTokens(rulesObject.getClass(), tokenName);
		if (tokenList == null || tokenList.isEmpty())
		{
			Logging.errorPrint("Unable to find a token to process " + tokenName + " for " + rulesObject.getClass());
			return null;
		}
		CDOMToken<T> token = tokenList.get(0);
		GeneralTagEditor<T> editor = new GeneralTagEditor<T>(tokenName, token, (T) rulesObject, context);
		editor.addActionListener(this);
		return editor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Refresh the current tags.
		buildTagList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save()
	{
		LoadContext context = Globals.getContext();
		Collection<String> result = context.unparse(rulesObject);
		String objectString = rulesObject.getDisplayName() + "\t"; 
		objectString += StringUtils.join(result, "\t");
		StringSelection selection = new StringSelection(objectString);
	    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
	}
}
