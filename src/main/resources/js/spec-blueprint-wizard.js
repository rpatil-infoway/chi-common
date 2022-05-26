/**
 * The JavaScript in this file determines how the Wizard for the Hello Blueprint blueprint will run.
 *
 * It is a belaboured example for illustrative purposes only, showing the different hooks that the Wizard API exposes:
 *  - pre-render
 *  - post-render
 *  - submit
 *
 * The example functions below show usage of the data that gets passed to these hooks
 */

AJS.bind('blueprint.wizard-register.ready', function() {
//	var invalidChar = [':', ';', ',', '.', ' ', '?', '&', '[', ']', '(', ')', '#', '^', '*', '@', '!', '<', '>'];
//	function containsInvalidChar(str) {
////		console.log("containsInvalidChar?");
//		for (var i = 0; i < invalidChar.length; i++) {
//			if (str.indexOf(invalidChar[i]) > -1) {
////				console.log("yes");
////				console.log(str.indexOf(invalidChar[i]));
//				return true;
//			}
//		}
////		console.log("no");
//		return false;
//	}
	
	function submitSpecSpace(e, state) {
		
		if (!state.pageData.organizations) {
			alert("Organization(s) not entered.");
			return false;
		}

		state.pageData.organizations = state.pageData.organizations.split(",");
		
		/*
		 * dropdown multiselects do not return properly, but grabbing the value via jQuery will 
		 * get the proper values
		 */
		state.pageData.jurisdictions = AJS.$("#jurisdictions-select").val();
		state.pageData.standards = AJS.$("#standards-select").val();
		
		// override if the free text fields are populated
		if (state.pageData.freetextDomain) {
			state.pageData.domain = state.pageData.freetextDomain;
		}
		if (!state.pageData.domain) {
			alert("Domain not selected.");
			return false;
		}
		if (state.pageData.freetextClinicalArea) {
			state.pageData.clinicalArea = state.pageData.freetextClinicalArea;
		}
		if (!state.pageData.clinicalArea) {
			alert("Clinical Area not selected.");
			return false;
		}
		
		if (!state.pageData.jurisdictions) {
			state.pageData.jurisdictions = [""];
		}
		
		// the following returns the multiple items as one string comma-separated,
		// so need to split it to get proper array representation
		if (state.pageData.freetextStandards) {
			state.pageData.standards = state.pageData.freetextStandards.split(",");
		} else {
			if (!state.pageData.standards) {
				state.pageData.standards = [""];
			}
		}
		if (state.pageData.metadata) {
			state.pageData.metadata = state.pageData.metadata.split(",");
		} else {
			state.pageData.metadata = [""];
		}
		
		console.log("Jurisdictions:");
		console.log(state.pageData.jurisdictions);
		console.log("Domain:");
		console.log(state.pageData.domain);
		console.log("Clinical Area:");
		console.log(state.pageData.clinicalArea);
		console.log("Standards: ");
		console.log(state.pageData.standards);
//		console.log(state.pageData.freetextStandards);
		console.log("Standards Version:");
		console.log(state.pageData.standardsVersion);
		console.log("Organizations: ");
		console.log(state.pageData.organizations);
		console.log("Additional Metadata: ");
		console.log(state.pageData.metadata);
		console.log(state);
		
//		if (containsInvalidChar(state.pageData.standardsVersion)) {
//			alert("Standards Version contains an invalid character.");
//			return false;
//		}

//		state.pageData.name = state.pageData.name  + " (" + state.pageData.jurisdictionKey + ")";
		state.pageData.name = state.pageData.name  + " - PRIVATE";
		state.pageData.ContentPageTitle = AJS.I18n.getText("confluence.samplespec.space.blueprint.homepage.suffix");
//        console.log(state);
//        console.log(e);
        
//		return false;
		return Confluence.SpaceBlueprint.CommonWizardBindings.submit(e, state);
    }
	
	function postRenderSpecSpace(e, state) { 
		AJS.$(".select2").auiSelect2({
			placeholder: "Click to select a value",
			allowClear: true
		});
		
		AJS.$(".select2[multiple='multiple']").auiSelect2({
			placeholder: "Click to select one or more values"
		});
		
		AJS.$(".select2-tags").auiSelect2({ 
			tags: true,
			tokenSeparators: [',']
		});
		return Confluence.SpaceBlueprint.CommonWizardBindings.postRender(e, state);
	}
	
    function preRenderSpecSpace(e, state) {
        state.soyRenderContext['atlToken'] = AJS.Meta.get('atl-token');
        state.soyRenderContext['showSpacePermission'] = false;
    }


    // Register wizard hooks
    Confluence.Blueprint.setWizard('ca.infoway.confluence.plugins.chi-common:chi-sample-specification-blueprint-item', function(wizard) {
    	wizard.on("pre-render.spaceBasicDetailsId", preRenderSpecSpace);
        wizard.on("post-render.spaceBasicDetailsId", postRenderSpecSpace);
        wizard.on("submit.spaceBasicDetailsId", submitSpecSpace);
    });

});

