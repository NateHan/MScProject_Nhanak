// dynamically loads a view by the URL of the router route to load and
// the id of the element to load the page into
function loadDoc(myUrl, id) {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById(id).innerHTML =
                this.responseText;
        }
    };
    xhttp.open("GET", myUrl, true);
    xhttp.send();
}

// On the project sliders click, it loads the URL of the controller method route, and then
// activates a dropdown menu, which animates once the information is loaded
$(document).ready(function () {

    $('.projSliderContent').hide();

    $('.projectSliders').on('click', function () {
        var targetArea = $(this).next('.projSliderContent');
        var urlToPass = $(targetArea).attr('url');
        targetArea.load(urlToPass, function () {
                $(this).slideToggle()
            }
        );
    });
});

// Listens for a submission of a sliderForm (in the dasboard)
// Linking with the jsRoutes method, it will post the form and return the result in the targeted area.
$(document).on('submit', '.sliderForms', function (event) {

    event.preventDefault();
    var content = $('#projectTitleInputBox').val();
    var content2 = $('#initNoteBox').val();
    window.alert("Made it to the ajax call with: " + content + '<<<>>> ' + content2);

    var myRoute = jsRoutes.controllers.DashboardController.postNewProject()
    $.ajax({

    });
    /*
    jsRoutes.controllers.DashboardController.postNewProject().ajax({
        success: function() { loadDoc('/dashboard/projects/sliderSubmitResponse/newProjSuccess', 'createNewProjectDiv') },
        fail: function() { loadDoc('/dashboard/projects/sliderSubmitResponse/newProjFail', 'createNewProjectDiv') }
    })
*/
});