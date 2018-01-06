import React from "react";
import PropTypes from "prop-types";
import { getDamageColor } from "./telemetryUtil.js";
import icon from "../image/brake2.png";

export default class BrakeDamage extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    const props = this.props;
    const frontDamage = props.frontDamage;
    const rearDamage = props.rearDamage;

    return (
      <svg style={{ width: "3rem", height: "75%" }} preserveAspectRatio="xMidYMid meet" viewBox="0 0 50 100">
        <rect x="13.5%" y="0.81rem" width="73%" height="2.28rem" fill={getDamageColor(frontDamage)} />
        <image x="0" y="0.8rem" width="100%" height="2.3rem" xlinkHref={icon} />
        <rect x="13.5%" y="3.21rem" width="73%" height="2.28rem" fill={getDamageColor(rearDamage)} />
        <image x="0" y="3.2rem" width="100%" height="2.3rem" xlinkHref={icon} />
        <text
          style={{ fontSize: "1rem", fontFamily: "'Inconsolata', monospace" }}
          x="50%"
          y="0.5rem"
          textAnchor="middle"
          dominantBaseline="middle"
          fill="#ffffff"
        >
          {frontDamage}%
        </text>
        <text
          style={{ fontSize: "1rem", fontFamily: "'Inconsolata', monospace" }}
          x="50%"
          y="6rem"
          textAnchor="middle"
          dominantBaseline="middle"
          fill="#ffffff"
        >
          {rearDamage}%
        </text>
      </svg>
    );
  }
}

BrakeDamage.propTypes = {
  frontDamage: PropTypes.string.isRequired,
  rearDamage: PropTypes.string.isRequired
};