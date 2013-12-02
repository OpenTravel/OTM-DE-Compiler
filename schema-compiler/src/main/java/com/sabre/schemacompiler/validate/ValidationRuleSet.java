/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Encapsulates a list of validation mappings to be applied prior to critical compiler
 * actions.  Rule sets may also be nested, allowing them to be extended by application
 * extensions or pluggable code generation tasks.
 * 
 * @author S. Livezey
 */
public class ValidationRuleSet {
	
	private List<ValidationRuleSet> nestedRuleSets = new ArrayList<ValidationRuleSet>();
	private List<ValidatorMapping<?>> validatorMappings = new ArrayList<ValidatorMapping<?>>();
	
	/**
	 * Default constructor.
	 */
	public ValidationRuleSet() {}
	
	/**
	 * Constructor used to assign a single nested set of validation rules.
	 * 
	 * @param nestedRuleSet  the validation rules to be extended by this set
	 */
	public ValidationRuleSet(ValidationRuleSet nestedRuleSet) {
		this.nestedRuleSets.add( nestedRuleSet );
	}
	
	/**
	 * Constructor used to assign a collection of nested validation rule sets to be
	 * combined and/or extended by this set.
	 * 
	 * @param nestedRuleSets  the collection of validation rule sets
	 */
	public ValidationRuleSet(Collection<ValidationRuleSet> nestedRuleSets) {
		this.nestedRuleSets.addAll( nestedRuleSets );
	}
	
	/**
	 * Returns the list of validator mappings that are directly declared by this rule
	 * set.  Mappings from nested rule sets are not returned by this method.
	 * 
	 * @return List<ValidatorMapping<?>>
	 */
	public List<ValidatorMapping<?>> getValidatorMappings() {
		return validatorMappings;
	}
	
	/**
	 * Assigns the list of validator mappings that are to be directly declared by this
	 * rule set.
	 * 
	 * @param validatorMappings  the validator mappings to assign
	 */
	public void setValidatorMappings(List<ValidatorMapping<?>> validatorMappings) {
		this.validatorMappings.clear();
		this.validatorMappings.addAll(validatorMappings);
	}
	
	/**
	 * Returns the list of all validator mappings available to this rule set, including
	 * those mappings inherited from nested rule sets.
	 * 
	 * @param <T>  the validatable target type
	 * @return List<Class<Validator<T>>>
	 */
	public <T extends Validatable> List<Class<Validator<T>>> getValidatorClasses(Class<T> targetClass) {
		List<Class<Validator<T>>> validatorClasses = new ArrayList<Class<Validator<T>>>();
		
		if (targetClass != null) {
			findValidatorClasses(targetClass, this, validatorClasses, new HashSet<ValidationRuleSet>());
		}
		return validatorClasses;
	}
	
	/**
	 * Recursive method that searches the given rule set for validators of the specified target type, as
	 * well as any nested rules sets associated with the one provided.
	 * 
	 * @param <T>  the validatable target type
	 * @param ruleSet  the rule set to analyze
	 * @param validatorClasses  the list of validator classes being compiled
	 * @param visitedRuleSets  the list of nested rule sets that have been visited (used to detect circular nesting)
	 */
	@SuppressWarnings("unchecked")
	private <T extends Validatable> void findValidatorClasses(Class<T> targetClass, ValidationRuleSet ruleSet,
			List<Class<Validator<T>>> validatorClasses, Collection<ValidationRuleSet> visitedRuleSets) {
		
		// First, recurse into the nested rule sets to find inherited validator mappings
		visitedRuleSets.add(ruleSet);
		
		for (ValidationRuleSet nestedRuleSet : ruleSet.nestedRuleSets) {
			if ((nestedRuleSet != null) && !visitedRuleSets.contains(nestedRuleSet)) {
				findValidatorClasses(targetClass, nestedRuleSet, validatorClasses, visitedRuleSets);
			}
		}
		
		// Now look for any applicable mappings declared by the current rule set
		for (ValidatorMapping<?> mapping : ruleSet.validatorMappings) {
			if (targetClass.equals(mapping.getTargetClass())) {
				Class<Validator<T>> validatorClass = ((ValidatorMapping<T>) mapping).getValidatorClass();
				
				if ((validatorClass != null) && !validatorClasses.contains(validatorClass)) {
					validatorClasses.add(validatorClass);
				}
			}
		}
	}
	
}
