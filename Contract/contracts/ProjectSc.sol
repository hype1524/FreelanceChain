// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

// Uncomment this line to use console.log
// import "hardhat/console.sol";

contract ProjectSc {
    struct Project {
        address payable customer;
        address payable freelancer;
        mapping (uint256 => uint256) mileStone;
        string urlContract;

    }

    mapping(uint256 => Project) projects;


    
}
