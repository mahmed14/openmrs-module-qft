/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.qft.web.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.qft.web.util.QFTKeys;
import org.openmrs.module.qft.web.util.TSVReader;
import org.openmrs.module.commonlabtest.LabTest;
import org.openmrs.module.commonlabtest.LabTestAttribute;
import org.openmrs.module.commonlabtest.LabTestAttributeType;
import org.openmrs.module.commonlabtest.LabTestType;
import org.openmrs.module.commonlabtest.api.CommonLabTestService;
import org.openmrs.module.commonlabtest.api.impl.CommonLabTestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * This class configured as controller using annotation and mapped with the URL of
 * 'module/basicmodule/basicmoduleLink.form'.
 */
@Controller
@RequestMapping(value = "module/qft/qft.form")
public class QFTFormController {
	
	/** Logger for this class and subclasses */
	protected final Log log = LogFactory.getLog(getClass());
	
	/** Success form view name */
	private final String SUCCESS_FORM_VIEW = "/module/qft/qft";
	
	@Autowired
	private CommonLabTestService commonLabTestService;
	
	/**
	 * Initially called after the formBackingObject method to get the landing form name
	 * 
	 * @return String form view name
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String showForm() {
		return SUCCESS_FORM_VIEW;
	}
	
	/**
	 * All the parameters are optional based on the necessity
	 * 
	 * @param httpSession
	 * @param anyRequestObject
	 * @param errors
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String onSubmit(HttpSession httpSession, @ModelAttribute("anyRequestObject") Object anyRequestObject,
	        BindingResult errors, @RequestParam(value = "file") MultipartFile file,ModelMap map) {
		
		//CommonLabTestService commonLabTestService =Context.getService(CommonLabTestService.class);
		List<String> errorsList=new ArrayList<String>();
		if (errors.hasErrors()) {
			// return error view
		}
		File convFile = new File( file.getOriginalFilename());
		 try {
			file.transferTo(convFile);
			List<Map<String, String>> results = TSVReader.parseTestFile(convFile);
			System.out.println(results);
			LabTestType qtLabTestType = commonLabTestService.getLabTestTypeByUuid("4f4c97c8-61c3-4c4e-82bc-ef3e8abe8ffa");
			List<LabTestAttributeType> attributeTypes = commonLabTestService.getLabTestAttributeTypes(qtLabTestType, false);
		
			
			for (Map<String,String> m: results) {
				List<LabTestAttribute> attributes=new ArrayList<LabTestAttribute>();
				String patientID=m.get(QFTKeys.SubjectID.toString());
				System.out.println("Patient ID :::" + patientID);
				if(patientID==null | patientID.isEmpty()) {
					System.out.println("Following record is not saved because of patient id being null");
					System.out.println(m);
					continue;
				}
				PatientIdentifierType identifierType = Context.getPatientService().getPatientIdentifierTypeByName("External ID");
				List<PatientIdentifierType> types=new ArrayList<>();
				types.add(identifierType);
				System.out.println("Patient Identifer Type :"+ identifierType.getName());
				
				List<Patient> patients=Context.getPatientService().getPatients(null, patientID, types, true);
				System.out.println("Patient Size ::: "+patients.size());
				if(patients.size()<1 || patients.get(0)==null  ) {
					System.out.println("Following record is not found with Externali ID ="+patientID);
					errorsList.add("Following record is not found with Externali ID ="+patientID);
				
					continue;
				}
				//Context.getPatientService().getPatientIdentifierTypeByName(arg0)tgetPatientsByIdentifier(patientID, false);
				List<LabTest> labtests = commonLabTestService.getLabTests(patients.get(0), false);
				
				System.out.println("labtests"+labtests);
				LabTest qfTest=null;
				for(LabTest lt:labtests) {
					if(lt.getLabTestType().equals(qtLabTestType)) {
						qfTest=lt;
					}
				}
				
				if(qfTest==null)
				{
					System.out.println("No test order found for Patient ID ="+patientID);
					errorsList.add("No test order found for Patient ID ="+patientID);
				
					continue;
				}
				for(LabTestAttributeType attributeType:attributeTypes) {
					LabTestAttribute att=new LabTestAttribute();
					att.setLabTest(qfTest);
					att.setAttributeType(attributeType);
					att.setAttributeTypeId(attributeType);
					System.out.print(attributeType.getName() +"		");
					if(attributeType.getName().equals(QFTKeys.RunNumber.toString())) {
						att.setValueReference(m.get(QFTKeys.RunNumber.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.RunDate.toString())) {
						att.setValueReference(m.get(QFTKeys.RunDate.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.NIL.toString())) {
						att.setValueReference(m.get(QFTKeys.NIL.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.TB1.toString())) {
						att.setValueReference(m.get(QFTKeys.TB1.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.TB2.toString())) {
						att.setValueReference(m.get(QFTKeys.TB2.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.MITOGEN.toString())) {
						att.setValueReference(m.get(QFTKeys.MITOGEN.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.TB1Nil.toString())) {
						att.setValueReference(m.get(QFTKeys.TB1Nil.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.TB2Nil.toString())) {
						att.setValueReference(m.get(QFTKeys.TB2Nil.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.MitogenNil.toString())) {
						att.setValueReference(m.get(QFTKeys.MitogenNil.toString()));
					}
					else if (attributeType.getName().equals(QFTKeys.Result.toString())) {
						att.setValueReference(m.get(QFTKeys.Result.toString()));
					}
					System.out.println(att.toString());
					//System.out.println(att.);
					attributes.add(att);
					
				}
				
			/*	String runNumber=m.get(QFTKeys.RunNumber.toString());
				String runDate=m.get(QFTKeys.RunDate.toString());
				String nill=m.get(QFTKeys.NIL.toString());
				String tb1=m.get(QFTKeys.TB1.toString());
				String tb2=m.get(QFTKeys.TB2.toString());
				String mitogen=m.get(QFTKeys.MITOGEN.toString());
				String tb1Nill=m.get(QFTKeys.TB1Nil.toString());
				String tb2Nill=m.get(QFTKeys.TB2Nil.toString());
				String mitogenNill=m.get(QFTKeys.MitogenNil.toString());
				String result=m.get(QFTKeys.Result.toString());*/
				System.out.println(attributes);
				System.out.println(attributes);
				commonLabTestService.saveLabTestAttributes(attributes);
				
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	
		
		 map.put("errorsList", errorsList);
		
		return SUCCESS_FORM_VIEW;
	}
}
