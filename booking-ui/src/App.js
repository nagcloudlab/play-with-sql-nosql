import logo from "./logo.svg";
import "./App.css";

import React, { useState } from "react";

function App() {
  const [poi, setPoi] = useState([]);

  const handleKeyup = (e) => {
    // if it is enter key
    if (e.keyCode === 13) {
      const poi = e.target.value;
      fetch(`http://localhost:8080/hotels/poi/${poi}`)
        .then((res) => res.json())
        .then((data) => {
          console.log(data);
          setPoi(data || []);
        });
    }
  };

  return (
    <div className="App">
      <h1>Booking UI</h1>
      <hr />
      <input type="text" placeholder="Enter POI" onKeyUp={handleKeyup} />
      <hr />
      <div>
        <h2>Hotels near</h2>
        <ul>
          {poi.map((hotel) => (
            <li>
              <a href="/hotels/{hotel.id}">
                {hotel.id} - {hotel.name}
              </a>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export default App;
