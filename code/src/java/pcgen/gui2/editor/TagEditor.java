/*
 * TagEditor.java
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
 * $Id: TagEditor.java 22711 2013-12-31 00:15:07Z jdempsey $
 */
package pcgen.gui2.editor;

import java.util.List;

import javax.swing.JPanel;

import pcgen.cdom.base.Loadable;

/**
 * TagEditor defines the interface to be used for tag editors.
 * 
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision: 22711 $
 */
public interface TagEditor<T extends Loadable>
{

	/**
	 * Verify the entered value and return any messages generated from 
	 * validation.
	 * @return The list of validation messages, empty if none.
	 */
	public abstract List<String> validate();

	/**
	 * Verify the entered value and if valid apply it to the rules object.
	 * @return The list of validation messages, empty if none.
	 */
	public abstract List<String> apply();

	/**
	 * @return The user interface for the editor, encapsulated in a panel.
	 */
	public abstract JPanel getEditorPanel();

	/**
	 * @return Were the editor contents valid when last validated?
	 */
	public abstract boolean isValid();

}