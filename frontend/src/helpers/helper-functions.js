import { USER_PROFIT_COEFFICIENT } from './helper-constants';
import Swal from "sweetalert2";
const BETS_KEY = 'bets'
const BALANCE_INFO = 'balance_info'
const ROLE = 'role'
const TOKEN_SALT = 'tokenSalt'
const TOKEN = 'token'
const USER_ID = 'userId'
const ADDRESS = 'address'
const TOKEN_EXPIRES_AT = 'tokenExpiresAt'


export const SUCCESS_ICON = 'success'
export const INFO_ICON = 'info'
export const ERROR_ICON = 'error'
export const toastMessage =  async (text, icon) => {
    await Swal.fire({
        position: "top-end",
        icon: icon,
        toast: true,
        showConfirmButton: false,
        timer: 2500,
        text: text,
        width: '300px'
    });
}

export const updateAddress = (address) => {
  window.sessionStorage.setItem(ADDRESS, address)
}
export const getTokenExpiresAt = () => {
  return window.sessionStorage.getItem(TOKEN_EXPIRES_AT)
}
export const getAddress = () => {
  return window.sessionStorage.getItem(ADDRESS)
}
export const getUserRole = () => {
    return window.sessionStorage.getItem(ROLE)
}
export const updateBalanceInfo = (balanceInfo) => {
  window.sessionStorage.setItem(BALANCE_INFO, JSON.stringify(balanceInfo))
}
export const getTokenSalt = () => {
  return window.sessionStorage.getItem(TOKEN_SALT)
}
export const updateTokenSalt = (tokenSalt) => {
  window.sessionStorage.setItem(TOKEN_SALT, tokenSalt)
}

export const updateToken = (token) => {
  window.sessionStorage.setItem(TOKEN, token)
}

export const getToken = () => {
  return window.sessionStorage.getItem(TOKEN)
}

export const getUserId = () => {
  return window.sessionStorage.getItem(USER_ID)
}

export const updateUserId = (userId) => {
  window.sessionStorage.setItem(USER_ID, userId)
}

export const updateRole = (role) => {
  window.sessionStorage.setItem(ROLE, role)
}

export const updateTokenExpiresAt = (tokenExpiresAt) => {
  window.sessionStorage.setItem(TOKEN_EXPIRES_AT, tokenExpiresAt)
}

export const getBalanceInfo = () => {
  const info = window.sessionStorage.getItem(BALANCE_INFO)
  if (info) return JSON.parse(info)
  return {}
}

export const getMaxBalance = () => {
    return getBalanceInfo()['ethBalance']
}

export const setMaxBalanceToZero = () => {
    const info = getBalanceInfo()
    info['ethBalance'] = "0.00000"
    window.sessionStorage.setItem(BALANCE_INFO, JSON.stringify(info))
}
export const getNotEnoughBalanceBet = (id) => {
  return window.sessionStorage.getItem(id)
}

export const updateNotEnoughBalanceBet = (id) => {
  window.sessionStorage.setItem(id, "true")
}

export const getBetsFromCache = () => {
  const jsonBets = window.sessionStorage.getItem(BETS_KEY)
  if (jsonBets){
    return JSON.parse(jsonBets)
  }
  return []
}

export const convertToFloatStats = (currentBet) => {
  const {
    ethWinStakeAmount,
    ethLossStakeAmount,
    betsForWinCount,
    betsForLossCount,
    ethTicketPrice
  } = currentBet;
  return {
    ethWinStakeAmount: parseFloat(ethWinStakeAmount),
    ethLossStakeAmount: parseFloat(ethLossStakeAmount),
    betsForWinCount: parseFloat(betsForWinCount),
    betsForLossCount: parseFloat(betsForLossCount),
    ethTicketPrice: parseFloat(ethTicketPrice)
  };
};
export const updateBetsCache = (bets) => {
  if (bets){
    window.sessionStorage.setItem(BETS_KEY, JSON.stringify(bets))
  }
}
export const convertToUsdt = (ethAmount, ethToUsd) => {
    if (isNaN(ethAmount)) {
        return "0.00";
    }
    const usdtAmountFloat = parseFloat(ethAmount) * ethToUsd;
    if (isNaN(usdtAmountFloat)) {
        return "0.00";
    }
    return usdtAmountFloat.toFixed(2);
};

export const calculatePrize = (usdPrize, peopleCount, totalPeopleCount, ethTicketPrice) => {
    if (totalPeopleCount <= 1) {
        return (ethTicketPrice * USER_PROFIT_COEFFICIENT).toFixed(2)
    }
    if (peopleCount === 0) return "0.00"
    return (usdPrize / peopleCount).toFixed(2)
}
export const convertToDate = (createdAt) => {
    const date = new Date(createdAt);
    return date.getFullYear() + '-' +
        (date.getMonth() + 1).toString().padStart(2, '0') + '-' +
        date.getDate().toString().padStart(2, '0') + ' ' +
        date.getHours().toString().padStart(2, '0') + ':' +
        date.getMinutes().toString().padStart(2, '0') + ':' +
        date.getSeconds().toString().padStart(2, '0');
}

export const isTokenExpired = (expirationTime) => {
    if (expirationTime == null) {
        return true
    }
    const expirationTimeDate = new Date(expirationTime);
    expirationTimeDate.setMinutes(expirationTimeDate.getMinutes() - 1)
    return new Date() > expirationTimeDate;
}