/*
 * GeneralTagEditor.java
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
 * Created on 31/12/2013
 *
 * $Id: GeneralTagEditor.java 22711 2013-12-31 00:15:07Z jdempsey $
 */
package pcgen.gui2.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import pcgen.cdom.base.Loadable;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.CDOMToken;
import pcgen.rules.persistence.token.ParseResult;
import pcgen.rules.persistence.token.ParseResult.Fail;
import pcgen.rules.persistence.token.ParseResult.QueuedMessage;

/**
 * GeneralTagEditor is a tag editor that can be used for any token. It provides 
 * a single text area where a user can enter the tag syntax and have it 
 * validated and applied.
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision: 22711 $
 */
public class GeneralTagEditor <T extends Loadable> implements TagEditor<T>
{
	private String tokenName;
	private CDOMToken<T> token;
	private T rulesObject;
	private boolean valid;
	private JTextArea textArea;
	private LoadContext context;
	private JPanel editorPane;
	private List<ActionListener> listenerList;

	/**
	 * Create a new editor instance.
	 * 
	 * @param tokenName The name of the token. (ie. the tag key)
	 * @param token The CDOMToken class that can be used to parse the entered syntax.
	 * @param rulesObject The loadable object the tag will be part of.
	 * @param context The current data load context.
	 */
	public GeneralTagEditor(String tokenName, CDOMToken<T> token, T rulesObject, LoadContext context)
	{
		this.tokenName = tokenName;
		this.token = token;
		this.rulesObject = rulesObject;
		this.context = context;
		this.listenerList = new ArrayList<ActionListener>();
	}
	
	protected List<String> validateCurrentValue()
	{
		List<String> errors = new ArrayList<String>();
		String tagText = textArea.getText();
		ParseResult parseResult = token.parseToken(context, rulesObject, tagText);
		valid = parseResult.passed();
		if (parseResult instanceof Fail)
		{
			QueuedMessage msg = ((Fail) parseResult).getError();
			if (msg != null)
			{
				errors.add(msg.message);
			}
		}
		return errors;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> validate()
	{
		List<String> result = validateCurrentValue();
		context.rollback();
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> apply()
	{
		List<String> result = validateCurrentValue();
		if (isValid())
		{
			context.commit();
			textArea.setEditable(false);
			notifyListeners();
		}
		else
		{
			context.rollback();
		}
		return result;
	}
	
	/**
	 * Let any listeners know about the successful apply action.
	 */
	private void notifyListeners()
	{
		ActionEvent e = new ActionEvent(this, 0, "APPLY");
		for (ActionListener listener : listenerList)
		{
			listener.actionPerformed(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JPanel getEditorPanel()
	{
		if (editorPane == null)
		{
			editorPane = new JPanel(new BorderLayout());
			editorPane.add(new JLabel(tokenName + ":"), BorderLayout.NORTH);
			textArea = new JTextArea();
			editorPane.add(textArea, BorderLayout.CENTER);
		}
		return editorPane;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isValid()
	{
		return valid;
	}
	
	public void addActionListener(ActionListener listener)
	{
		listenerList.add(listener);
	}
	
	public void removeActionListener(ActionListener listener)
	{
		listenerList.remove(listener);
	}
}