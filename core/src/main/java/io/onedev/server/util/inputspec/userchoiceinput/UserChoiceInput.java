package io.onedev.server.util.inputspec.userchoiceinput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import io.onedev.server.OneDev;
import io.onedev.server.util.facade.UserFacade;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.util.inputspec.userchoiceinput.choiceprovider.ChoiceProvider;
import io.onedev.server.util.inputspec.userchoiceinput.choiceprovider.IssueReaders;
import io.onedev.server.util.inputspec.userchoiceinput.choiceprovider.TeamUsers;
import io.onedev.server.util.inputspec.userchoiceinput.defaultvalueprovider.DefaultValueProvider;
import io.onedev.server.util.inputspec.userchoiceinput.defaultvalueprovider.SpecifiedDefaultValue;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NameOfEmptyValue;
import jersey.repackaged.com.google.common.collect.Lists;

@Editable(order=150, name=InputSpec.USER)
public class UserChoiceInput extends InputSpec {
	
	private static final long serialVersionUID = 1L;

	private ChoiceProvider choiceProvider = new IssueReaders();

	private DefaultValueProvider defaultValueProvider;
	
	@Editable(order=1000, name="Available Choices")
	@NotNull(message="may not be empty")
	public ChoiceProvider getChoiceProvider() {
		return choiceProvider;
	}

	public void setChoiceProvider(ChoiceProvider choiceProvider) {
		this.choiceProvider = choiceProvider;
	}

	@Editable(order=1100, name="Default Value")
	@NameOfEmptyValue("No default value")
	public DefaultValueProvider getDefaultValueProvider() {
		return defaultValueProvider;
	}

	public void setDefaultValueProvider(DefaultValueProvider defaultValueProvider) {
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	public List<String> getPossibleValues() {
		List<String> possibleValues = new ArrayList<>();
		if (OneDev.getInstance(Validator.class).validate(getChoiceProvider()).isEmpty()) {
			for (UserFacade user: getChoiceProvider().getChoices(true))
				possibleValues.add(user.getName());
		}
		return possibleValues;
	}

	@Override
	public String getPropertyDef(Map<String, Integer> indexes) {
		int index = indexes.get(getName());
		StringBuffer buffer = new StringBuffer();
		appendField(buffer, index, "String");
		appendCommonAnnotations(buffer, index);
		if (!isAllowEmpty())
			buffer.append("    @NotEmpty\n");
		appendChoiceProvider(buffer, index, "@UserChoice");
		appendMethods(buffer, index, "String", choiceProvider, defaultValueProvider);
		
		return buffer.toString();
	}

	@Editable
	@Override
	public boolean isAllowMultiple() {
		return false;
	}

	@Override
	public void onRenameUser(String oldName, String newName) {
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(oldName))
				specifiedDefaultValue.setValue(newName);
		}
	}

	@Override
	public boolean onDeleteUser(String userName) {
		if (super.onDeleteUser(userName))
			return true;
		if (defaultValueProvider instanceof SpecifiedDefaultValue) {
			SpecifiedDefaultValue specifiedDefaultValue = (SpecifiedDefaultValue) defaultValueProvider;
			if (specifiedDefaultValue.getValue().equals(userName))
				defaultValueProvider = null;
		}
		return false;
	}

	@Override
	public void onRenameGroup(String oldName, String newName) {
		if (choiceProvider instanceof TeamUsers) {
			TeamUsers groupUsers = (TeamUsers) choiceProvider;
			if (groupUsers.getTeamName().equals(oldName))
				groupUsers.setTeamName(newName);
		}
	}

	@Override
	public boolean onDeleteGroup(String groupName) {
		if (super.onDeleteGroup(groupName))
			return true;
		if (choiceProvider instanceof TeamUsers) {
			TeamUsers groupUsers = (TeamUsers) choiceProvider;
			if (groupUsers.getTeamName().equals(groupName))
				return true;
		}
		return false;
	}
	
	@Override
	public Object convertToObject(List<String> strings) {
		return strings.iterator().next();
	}

	@Override
	public List<String> convertToStrings(Object value) {
		return Lists.newArrayList((String) value);
	}

}