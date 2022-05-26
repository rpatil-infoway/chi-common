/**
 * The JavaScript in this file determines how the Wizard for the Hello Blueprint blueprint will run.
 *
 * It is a belaboured example for illustrative purposes only, showing the different hooks that the Wizard API exposes:
 *   MUST INCLUDE THE FOLLOWING
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
	
	function submitEmptySpace(e, state) {
//		state.pageData.name = state.pageData.name  + " (" + state.pageData.jurisdictionKey + ")";
		state.pageData.name = state.pageData.name  + " - PRIVATE";
		state.pageData.ContentPageTitle = AJS.I18n.getText("confluence.empty.space.with.versioning.blueprint.homepage.suffix");
//        console.log(state);
//        console.log(e);
        
//		return false;
		return Confluence.SpaceBlueprint.CommonWizardBindings.submit(e, state);
    }
	
    function preRenderEmptySpace(e, state) {
        state.soyRenderContext['atlToken'] = AJS.Meta.get('atl-token');
        state.soyRenderContext['showSpacePermission'] = false;
    }
    
    function postRenderEmptySpace(e, state) { 
		return Confluence.SpaceBlueprint.CommonWizardBindings.postRender(e, state);
	}

    // Register wizard hooks
    Confluence.Blueprint.setWizard('ca.infoway.confluence.plugins.chi-common:chi-empty-with-versioning-space-blueprint-item', function(wizard) {
    	wizard.on("pre-render.spaceBasicDetailsId", preRenderEmptySpace);
    	wizard.on("post-render.spaceBasicDetailsId", postRenderEmptySpace);
        wizard.on("submit.spaceBasicDetailsId", submitEmptySpace);
    });

});
