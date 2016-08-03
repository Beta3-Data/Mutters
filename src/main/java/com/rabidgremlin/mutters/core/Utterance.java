package com.rabidgremlin.mutters.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabidgremlin.mutters.util.Utils;

public class Utterance
{

	private String template;
	private CleanedInput tokens;
	// private HashMap<Integer, String> slotNames = new HashMap<Integer,
	// String>();
	private HashSet<String> slotNames = new HashSet<String>();

	private Pattern matchPattern;

	public Utterance(String template)
	{
		this.template = template;
		tokens = InputCleaner.cleanInput(template);

		List<String> cleanedTokens = tokens.getCleanedTokens();

		String regexStr = "^";

		for (String token : cleanedTokens)
		{
			if (token.startsWith("{") && token.endsWith("}"))
			{
				String slotName = token.substring(1, token.length() - 1);

				regexStr += "(?<" + slotName + ">.*) ";

				slotNames.add(slotName);
			}
			else
			{
				regexStr += Pattern.quote(token) + " ";
			}
		}

		//System.out.println(regexStr);
		matchPattern = Pattern.compile(regexStr.trim(), Pattern.CASE_INSENSITIVE);
	}

	public String getTemplate()
	{
		return template;
	}

	// NOTE input should be cleaned
	public UtteranceMatch matches(CleanedInput input, Slots slots, Context context)
	{
		String inputString = StringUtils.join(input.getCleanedTokens(), ' ');

		Matcher match = matchPattern.matcher(inputString);

		if (!match.find())
		{
			return new UtteranceMatch(false);
		}

		UtteranceMatch theMatch = new UtteranceMatch(true);

		for (String slotName : slotNames)
		{
			Slot slot = slots.getSlot(slotName);

			if (slot == null)
			{
				throw new IllegalStateException("Cannot find slot '" + slotName + " reference by utterace '" + template + "'");
			}

			String cleanedMatchString = match.group(slotName);
			@SuppressWarnings("unchecked")
			List<String> matchedTokens = Arrays.asList(cleanedMatchString.split(" "));

			int matchPos = Collections.indexOfSubList(input.getCleanedTokens(), matchedTokens);
			if (matchPos == -1)
			{
				throw new IllegalStateException("Was unable to find '" + cleanedMatchString + "' in '" + inputString + "'");
			}

			List<String> orginalTokens = new ArrayList<>();
			for (int loop = matchPos; loop < matchedTokens.size() + matchPos; loop++)
			{
				orginalTokens.add(input.getOriginalTokens().get(loop));
			}

			SlotMatch slotMatch = slot.match(StringUtils.join(orginalTokens, ' '), context);

			if (slotMatch != null)
			{
				theMatch.getSlotMatches().put(slot, slotMatch);
			}
			else
			{
				return new UtteranceMatch(false);
			}
		}

		return theMatch;
	}

	@Override
	public String toString()
	{
		return "Utterance [template=" + template + ", tokens=" + tokens + "]";
	}

}