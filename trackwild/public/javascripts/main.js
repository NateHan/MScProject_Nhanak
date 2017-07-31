function loadDoc(myUrl) {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            document.getElementById("toolOptions").innerHTML =
                this.responseText;
        }
    };
    xhttp.open("GET", myUrl, true);
    xhttp.send();
}

$(document).ready(function(){

    $('.projSliderContent').hide();

    $('.projectSliders').on('click', function(){
        $(this).next('.projSliderContent').slideToggle("slow");
    });
});