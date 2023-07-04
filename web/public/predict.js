const { $ } = window;
let predictionId = Date.now();
$(function() {
    var socket = io.connect('/', { 'forceNew': true });

    function renderSpinner() {
        var html = `<img src="spinner.gif" className="spinner"/>`;
        document.getElementById('messages').innerHTML = html;

    }

    function renderAlert(data, type = "primary") {
        var html = `<div <div class="notification is-danger is-light">
		<button class="delete"></button>
		  ${data}
		</div>`;
        document.getElementById('messages').innerHTML = html;
    }

    document.addEventListener('DOMContentLoaded', () => {
        (document.querySelectorAll('.notification .delete') || []).forEach(($delete) => {
            const $notification = $delete.parentNode;

            $delete.addEventListener('click', () => {
                $notification.parentNode.removeChild($notification);
            });
        });
    });

    function renderResult(data) {
        var html = `<div id="result-container">
            <div>Porcentaje ocupación</div>
			<div id="result">0%</div>
		</div>`;
        document.getElementById('messages').innerHTML = html;
        let countdown = 0;
        let interval = setInterval(() => {
            if (countdown < data) {
                countdown++;
                document.getElementById('messages').innerHTML = `<div id="result-container">
                    <div>Porcentaje ocupación</div>
					<div id="result">~${countdown}%</div>
				</div>`;
            } else {
                interval = null;
            }
        }, 10);
        $('#result');
    }

    socket.on('messages', function(action) {
        try {
            switch (action.type) {
                case "CONFIRMATION":
                    renderSpinner();
                    break;
                case "ERROR":
                    renderAlert(action.payload.msg, "danger");
                    break;
                case "PREDICTION":
                    console.log(action.payload.predictionId, predictionId)
                    if (predictionId === (action.payload.predictionId)) {
                        renderResult(action.payload.predictionValue);
                    }
                    break;
                default:
                    console.error("Unrecognized message type");
            }
        } catch (e) {
            console.error(e)
        }
    });


    const updateDate = (date) => {
        $("#date").val(date);
        $("#start").focus();
    };
    const getQueryStringValue = (key) => decodeURIComponent(window.location.search.replace(new RegExp(`^(?:.*[&\\?]${encodeURIComponent(key).replace(/[\.\+\*]/g, "\\$&")}(?:\\=([^&]*))?)?.*$`, "i"), "$1"));

    const calendarOptions = {
        "lang": "es-ES"
    }
    const calendars = bulmaCalendar.attach('[type="date"]', calendarOptions);

    // Loop on each calendar initialized
    calendars.forEach(calendar => {
        // Add listener to select event
        calendar.on('select', date => {
            updateDate(date.data.value())
            console.log(date.data.value());
        });
    });

    var today = new Date();
    var date = (today.getMonth() + 1) + '/' + today.getDate() + '/' + today.getFullYear()
    updateDate(date);

    $("#newForm").on("submit", function() {
        const dateSubmitted = new Date($("#date").val());
        const time = parseInt($('#hourSelect').val());
        const name = $('#nameSelect').val();
        predictionId = "p-" + Date.now();
        const msg = {
            name: name,
            year: dateSubmitted.getUTCFullYear(),
            month: dateSubmitted.getUTCMonth() + 1,
            day: dateSubmitted.getUTCDate() + 1,
            weekday: dateSubmitted.getDay() + 1,
            time: time,
            predictionId: predictionId
        }
        socket.emit("predict", msg);
        return false;
    });
});