<html>
  <head>
      <title>Circuit Breakers</title>
      <style type="text/css">
          .breaker-table {
              border-collapse: collapse;
              font-family: Helvetica,Arial,Helmet,Freesans,sans-serif;
          }

          .breaker-table th, .breaker-table td {
              padding: 5px;
              vertical-align: top;
              border-right: 1px black dotted;
          }

          .breaker-table tr.alt {
              background-color: #d3d3d3;
          }

          .breaker-table tr.reg {
              background-color: #a9a9a9
          }

          .breaker-table th{
              background-color: black;
              color:#f5f5f5;
          }

          .breaker-table td.open{
              color: red;
          }

          .breaker-table td.close{
              color: green;
          }

          .center {
              text-align: center;
          }

      </style>
  </head>
  <body>
        <g:if test="${breakers}">
            <table class="breaker-table">
                <thead>
                    <tr>
                        <th>Toggle</th>
                        <th>State</th>
                        <th>Name</th>
                        <th>Failure Count / Threshold</th>
                        <th>Time Until Retry</th>
                        <th>Last Open Time</th>
                        <th>Non-Tripping Exceptions</th>
                    </tr>
                </thead>
                <tbody>
                   <g:each in="${breakers}" var="breaker" status="count">
                       
                        <tr class="${count % 2 == 0 ? 'alt' : 'reg'}">
                            <g:form name="${breaker.name}" action="toggle" controller="breaker" id="${breaker.name}">
                                <td><g:submitButton name="toggle" value="toggle"/></td>
                                <td class="${breaker.state.name == "Open" ? 'open' : 'close'}">${breaker.state.name}</td>
                                <td>${breaker.name}</td>
                                <td class="center">${breaker.failureCount} / ${breaker.failureThreshold}</td>
                                <td class="center">${breaker.timeUntilRetry} ms </td>
                                <td>
                                   <g:if test="${breaker.lastOpenedTime}">
                                       <g:formatDate date="${new Date(breaker.lastOpenedTime)}" format="MM-dd-yyyy @ HH:mm:ss.SSS z" />
                                   </g:if>
                                   <g:else>
                                       Has not been tripped yet
                                   </g:else> <br/>

                                <td>
                                    <g:if test="${breaker.nonTrippingExceptions.size() > 0}">
                                        <ul>
                                            <g:each in="${breaker.nonTrippingExceptions}" var="ex">
                                                <li>
                                                    ${ex}
                                                </li>
                                            </g:each>
                                        </ul>
                                    </g:if>
                                    <g:else>
                                        None
                                    </g:else>
                                </td>
                            </g:form>
                       </tr>
                    </g:each>
                </tbody>

            </table>

        </g:if>

        <g:else>
            <h1>There are no Circuit Breakers configured for this application</h1>
        </g:else>
  </body>
</html>