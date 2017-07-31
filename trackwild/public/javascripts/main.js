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

$(document).ready(function () {

    $('.projSliderContent').hide();

    $('.projectSliders').on('click', function () {
        var targetArea = $(this).next('.projSliderContent');
        var urlToPass = $(targetArea).attr('url');
        targetArea.load(urlToPass, function(){
                $(this).slideToggle()
            }
        );
    });
});