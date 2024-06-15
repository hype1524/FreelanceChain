// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;
import "@openzeppelin/contracts/token/ERC721/extensions/ERC721URIStorage.sol";
import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
// Uncomment this line to use console.log
// import "hardhat/console.sol";

contract UserContract is ERC721URIStorage{
    struct UserEntity {
        string id;
        address payable wallet;
        balance
    }

    mapping(uint256 => Project) user;

    
    
}