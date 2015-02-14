/*
 * LstEditorFacade.java
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
 * $Id: LstEditorFacade.java 22711 2013-12-31 00:15:07Z jdempsey $
 */
package pcgen.gui2.editor;

import pcgen.cdom.base.CDOMObject;
import pcgen.facade.util.ListFacade;

/**
 * LstEditorFacade defines the model to be used by the LSTEditorDialog. This 
 * contains all interaction the dialog will have with the PCGen Rules Store. Each 
 * instance is specific to a rules object being edited.  
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision: 22711 $
 */
public interface LstEditorFacade
{

	/**
	 * @return A list of names of tokens which can be added to the current 
	 * rules object.
	 */
	public abstract ListFacade<String> getValidTokens();

	/**
	 * Provide the unparsed LST syntax of the rules object as a list of tags.
	 * @return The list of LST tags which make up the rules object. 
	 */
	public abstract ListFacade<String> getObjectTags();

	/**
	 * @return The name of the rules object.
	 */
	public abstract String getName();

	/**
	 * The editor which can be used by the user to enter a value for the 
	 * particular token for the current rules object. 
	 * @param tokenName The token name (i.e. tag key)
	 * @return The editor to be used, or null if the token request is not valid.
	 */
	public abstract <T extends CDOMObject> TagEditor<T> getEditorForToken(
		String tokenName);

	/**
	 * Save the rules object.
	 */
	public abstract void save();

}