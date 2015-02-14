/*
 * LstEditorDialog.java
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
 * $Id: LstEditorDialog.java 22711 2013-12-31 00:15:07Z jdempsey $
 */
package pcgen.gui2.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import pcgen.cdom.base.CDOMObject;
import pcgen.core.Globals;
import pcgen.core.Race;
import pcgen.gui2.tools.FlippingSplitPane;
import pcgen.gui2.tools.Icons;
import pcgen.gui2.tools.InfoPane;
import pcgen.gui2.util.FacadeListModel;
import pcgen.gui2.util.JListEx;

/**
 * LstEditorDialog is a dialog for editing a rules object. It allows the user to 
 * add, modify and delete tags for the object as well as rename the object. 
 * 
 * The current implementation is only a proof of concept that provides a random
 * Race for editing. 
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision: 22711 $
 */
@SuppressWarnings("serial")
public class LstEditorDialog extends JDialog
{
	private final JTextField objectName;
	private final FacadeListModel<String> availTokenModel;
	private final FacadeListModel<String> objectTagModel;
	private final JListEx availTokenList;
	private final JListEx tagList;
	private final JPanel tagEditorPane;  
	private final InfoPane infoPane;
	private final CDOMObject editObject;
	private final LstEditorFacade editorFacade;
	private final NewTagAction newTagAction;
	private final ValidateAction validateAction;
	private final ApplyAction applyAction;
	private final CancelAction cancelAction;
	private final EditAction editAction;
	private final DeleteAction deleteAction;
	private final SaveAction saveAction;
	private final CancelObjectAction cancelObjectAction;

	private TagEditor<? extends CDOMObject> editor;
	
	/**
	 * Create a new LstEditorDialog instance.
	 * @param frame The parent frame.
	 */
	public LstEditorDialog(Frame frame)
	{
		super(frame, false);
		
		this.objectName = new JTextField(40);
		this.availTokenList = new JListEx();
		this.tagList = new JListEx();
		this.tagEditorPane = new JPanel(new BorderLayout());
		this.infoPane = new InfoPane("Messages");
		this.availTokenModel = new FacadeListModel<String>();
		this.objectTagModel = new FacadeListModel<String>();
		
		this.newTagAction = new NewTagAction();
		this.validateAction = new ValidateAction();
		this.applyAction = new ApplyAction();
		this.cancelAction = new CancelAction();
		this.editAction = new EditAction();
		this.deleteAction = new DeleteAction();
		this.saveAction = new SaveAction();
		this.cancelObjectAction = new CancelObjectAction();

		Collection<Race> races = Globals.getContext().getReferenceContext().getConstructedCDOMObjects(Race.class);
		if (races.size() < 29000)
		{
			this.editObject = new Race();
			editObject.setName("Editor Test");
		}
		else
		{
			Iterator<Race> raceIter = races.iterator();
			raceIter.next(); // Skip over None selected.
			this.editObject = raceIter.next();
		}
		this.editorFacade = new LstEditorFacadeImpl(editObject);
		prepareEditor();
		initComponents();
		pack();
		
		newTagAction.install();
	}

	/**
	 * Layout the dialog. 
	 */
	private void initComponents()
	{
		setTitle("LST Editor - POC");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowClosed(WindowEvent e)
			{
				//detach listeners from the editor window
				newTagAction.uninstall();
			}

		});
		Container pane = getContentPane();
		pane.setLayout(new BorderLayout());

		JPanel titlePanel = new JPanel();
		titlePanel.add(new JLabel("Race:"));
		titlePanel.add(objectName);
		pane.add(titlePanel, BorderLayout.NORTH);

		// Top component is the left/right split of token list to object
		FlippingSplitPane leftRightPane = new FlippingSplitPane("LstEditorLeftRight");
		leftRightPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		JPanel availTokenPanel = new JPanel(new BorderLayout());
		availTokenPanel.add(new JLabel("Available Tokens"), BorderLayout.NORTH);
		availTokenPanel.add(new JScrollPane(availTokenList), BorderLayout.CENTER);
		Box availBox = Box.createHorizontalBox();
		availBox.add(Box.createHorizontalGlue());
		JButton addButton = new JButton(newTagAction);
		addButton.setHorizontalTextPosition(SwingConstants.LEADING);
		availBox.add(addButton);
		availBox.add(Box.createHorizontalStrut(5));
		availBox.setBorder(new EmptyBorder(0,  0, 5, 0));
		availTokenPanel.add(availBox, BorderLayout.SOUTH);
		leftRightPane.setLeftComponent(availTokenPanel);

		FlippingSplitPane objectEditPane = new FlippingSplitPane("LstEditorObjectEdit");
		objectEditPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		JPanel existingTagsPanel = new JPanel(new BorderLayout());
		existingTagsPanel.add(new JLabel("Existing Tags"), BorderLayout.NORTH);
		existingTagsPanel.add(new JScrollPane(tagList), BorderLayout.CENTER);
		Box rulesObjectButtonBox = Box.createHorizontalBox();
		rulesObjectButtonBox.add(Box.createHorizontalGlue());
		JButton editButton = new JButton(editAction);
		rulesObjectButtonBox.add(editButton);
		rulesObjectButtonBox.add(Box.createHorizontalStrut(5));
		JButton deleteButton = new JButton(deleteAction);
		rulesObjectButtonBox.add(deleteButton);
		rulesObjectButtonBox.add(Box.createHorizontalStrut(5));
		JButton saveButton = new JButton(saveAction);
		rulesObjectButtonBox.add(saveButton);
		rulesObjectButtonBox.add(Box.createHorizontalStrut(5));
		JButton cancelObjectButton = new JButton(cancelObjectAction);
		rulesObjectButtonBox.add(cancelObjectButton);
		rulesObjectButtonBox.add(Box.createHorizontalStrut(5));
		existingTagsPanel.add(rulesObjectButtonBox, BorderLayout.SOUTH);
		objectEditPane.setTopComponent(existingTagsPanel);
		
		Box editorButtonBox = Box.createHorizontalBox();
		editorButtonBox.add(Box.createHorizontalGlue());
		JButton validateButton = new JButton(validateAction);
		editorButtonBox.add(validateButton);
		editorButtonBox.add(Box.createHorizontalStrut(5));
		JButton applyButton = new JButton(applyAction);
		editorButtonBox.add(applyButton);
		editorButtonBox.add(Box.createHorizontalStrut(5));
		JButton cancelButton = new JButton(cancelAction);
		editorButtonBox.add(cancelButton);
		editorButtonBox.add(Box.createHorizontalGlue());
		editorButtonBox.setBorder(new EmptyBorder(0,  0, 5, 0));
		tagEditorPane.add(editorButtonBox, BorderLayout.SOUTH);
		objectEditPane.setBottomComponent(tagEditorPane);
		
		leftRightPane.setRightComponent(objectEditPane);


		// Build the top/bottom split pane
		FlippingSplitPane mainPane = new FlippingSplitPane("LstEditorMain");
		pane.add(mainPane, BorderLayout.CENTER);
		mainPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainPane.setTopComponent(leftRightPane);
		mainPane.setBottomComponent(infoPane);
		mainPane.setResizeWeight(.75);
		
	}

	/**
	 * Populate the dialog with data from the facade.
	 */
	private void prepareEditor()
	{
		objectName.setText(editorFacade.getName());
		availTokenModel.setListFacade(editorFacade.getValidTokens());
		availTokenList.setModel(availTokenModel);
		objectTagModel.setListFacade(editorFacade.getObjectTags());
		tagList.setModel(objectTagModel);
	}

	/**
	 * Update the message area to indicate the result of validation.
	 * @param isValid Is the entered text currently valid?
	 * @param messages The message returned from parsing.
	 */
	private void notifyValidationStatus(boolean isValid, List<String> messages)
	{
		String messageText = isValid ? "Valid<br>" : "<b><font color='red'>Invalid</font></b><br>";
		if (!messages.isEmpty())
		{
			messageText += StringUtils.join(messages, "<br>");
		}
		infoPane.setText(messageText);
	}

	/**
	 * Action to start editing a new tag to be added to the rules object. 
	 * Requires a token name to be selected from the available list.
	 */
	private class NewTagAction extends AbstractAction
	{

		public NewTagAction()
		{
			super("New Tag");
			putValue(SMALL_ICON, Icons.Forward16.getImageIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			String tokenName = (String) availTokenList.getSelectedValue();
			if (StringUtils.isEmpty(tokenName) || tokenName.startsWith("---"))
			{
				return;
			}
			editor = editorFacade.getEditorForToken(tokenName);
			if (editor == null)
			{
				return;
			}
			
			String messageText = "";
			infoPane.setText(messageText);
			
			JPanel editorPanel = editor.getEditorPanel();
			tagEditorPane.add(editorPanel, BorderLayout.CENTER);
			tagEditorPane.revalidate();
			tagEditorPane.repaint();
		}
		
		public void install()
		{
			availTokenList.addActionListener(this);
		}
		
		public void uninstall()
		{
			availTokenList.removeActionListener(this);
		}

	}

	/**
	 * Edit the selected existing tag. 
	 */
	private class EditAction extends AbstractAction
	{

		public EditAction()
		{
			super("Edit");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
		}
	}

	/**
	 * Delete the selected existing tag. 
	 */
	private class DeleteAction extends AbstractAction
	{

		public DeleteAction()
		{
			super("Delete");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
		}
	}

	/**
	 * Save the updated rules object. 
	 */
	private class SaveAction extends AbstractAction
	{

		public SaveAction()
		{
			super("Save");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			editorFacade.save();
		}
	}

	/**
	 * Close the updated rules object without saving. 
	 */
	private class CancelObjectAction extends AbstractAction
	{

		public CancelObjectAction()
		{
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
		}
	}
		
	/**
	 * Validate the tag being entered. This does not apply the result to the 
	 * object but just checks for validity.
	 */
	private class ValidateAction extends AbstractAction
	{

		public ValidateAction()
		{
			super("Validate");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (editor == null)
			{
				return;
			}
			
			List<String> messages = editor.validate();
			notifyValidationStatus(editor.isValid(), messages);
		}
	}

	/**
	 * Validate the tag being entered and if it is valid, add it to the 
	 * rules object. 
	 */
	private class ApplyAction extends AbstractAction
	{

		public ApplyAction()
		{
			super("Apply");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (editor == null)
			{
				return;
			}
			
			List<String> messages = editor.apply();
			if (editor.isValid())
			{
				infoPane.setText("Applied");
				tagEditorPane.remove(editor.getEditorPanel());
				tagEditorPane.revalidate();
				tagEditorPane.repaint();
			}
			else
			{
				notifyValidationStatus(editor.isValid(), messages);
			}
		}
	}

	/**
	 * Cancel the tag entry making no changes to the rules object.
	 */
	private class CancelAction extends AbstractAction
	{

		public CancelAction()
		{
			super("Cancel");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (editor == null)
			{
				return;
			}
			
			String messageText = "Cancelled";
			infoPane.setText(messageText);
			tagEditorPane.remove(editor.getEditorPanel());
			tagEditorPane.revalidate();
			tagEditorPane.repaint();
		}
	}
	
}
