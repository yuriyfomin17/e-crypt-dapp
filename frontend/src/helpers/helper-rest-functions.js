import axios from 'axios';
import useSWRSubscription from 'swr/subscription';
import useSWRImmutable from 'swr/immutable';
import {
  BE_MESSAGE_TYPE,
  BE_RESPONSES_MESSAGES,
  BET_STATUSES, GAME_FINISHED_MESSAGE, TOAST_BALANCE_CHANGE_MESSAGE, TOAST_GENERAL_ERROR_MESSAGE,
  UPDATE_TYPE
} from './helper-constants';
import {
  ERROR_ICON,
  getAddress,
  getBetsFromCache,
  getNotEnoughBalanceBet, getToken, getUserId, INFO_ICON, SUCCESS_ICON, toastMessage, updateBalanceInfo,
  updateBetsCache,
  updateNotEnoughBalanceBet
} from './helper-functions';

const getHeaders = () => {
  return {
    headers: {
      'userid': getUserId(),
      'Authorization': 'Bearer ' + getToken(),
      'Content-Type': 'application/json',
      'Access-Control-Allow-Origin': process.env.NEXT_PUBLIC_FRONT_END_URL,
      'address': getAddress()
    }
  };
};
export const refreshToken = async () => {
  return await axios.post(process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/common/refresh', {},
    getHeaders())
                    .then(res => res.data)
                    .catch(() => window.sessionStorage.clear());
};

export const signUser = async (token) => {
  const userDataUrl = process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/api/auth/login';
  return await axios.get(userDataUrl, {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token
    }
  }).then(res => {
    const bets = res.data['betDtos'];
    updateBetsCache(bets);
    return {
      address: token,
      ...res.data
    };
  }).catch(() => false);
};

export const singOutUser = async () => {
  await axios.get(process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/api/auth/logout', getHeaders())
             .then(() => window.sessionStorage.clear())
             .catch(() => window.sessionStorage.clear());
};

export const createDeposit = async (hash) => {
  const headers = getHeaders();
  await axios.post(process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/common/create/deposit', {
    hash,
    userId: getUserId()
  }, headers).then(res => handleResponse(res))
             .catch(() => {
                  toastMessage(BE_RESPONSES_MESSAGES.GENERAL_ERROR, ERROR_ICON)
             }
                 
             );
};

export const getUserBalance = async () => {
  const headers = getHeaders();
  return await axios.get(process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/common/account/userData', headers)
             .then(res => res.data)
             .catch(() => toastMessage(BE_RESPONSES_MESSAGES.GENERAL_ERROR, ERROR_ICON));
}

export const withdrawFunds = async ( signature, ethAmount) => {
  const url = process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/common/create/withdraw';

  await axios.post(url, {
    userId: getUserId(),
    signature,
    ethAmount,
    address: getAddress()
  }, getHeaders()).then(res => handleResponse(res))
             .catch(() => toastMessage(BE_RESPONSES_MESSAGES.GENERAL_ERROR, ERROR_ICON));
};

const handleResponse = (res) => {
  const type = res['data']['type'];
  const responseType = res['data']['responseType'];
  const message = BE_RESPONSES_MESSAGES[responseType];
  if (!message) {
    toastMessage(BE_RESPONSES_MESSAGES.GENERAL_ERROR, ERROR_ICON);
    return;
  }
  switch (type) {
    case BE_MESSAGE_TYPE.SUCCESS:
        toastMessage(message, SUCCESS_ICON);
      break;
    case BE_MESSAGE_TYPE.ERROR:
        toastMessage(message, ERROR_ICON);
      break;
    default:
      toastMessage(BE_RESPONSES_MESSAGES.GENERAL_ERROR, ERROR_ICON);
  }
};

export const UserTransactions = (path) => {
  const url = process.env.NEXT_PUBLIC_BACKEND_URL + path;
  const transactionsFetcher = async (url) => await axios.get(url,
    getHeaders()).then(res => res.data);
  const { data, error, isLoading, mutate } = useSWRImmutable(url, transactionsFetcher);
  return {
    transactions: data ? data : [],
    isLoading,
    isError: error,
    txMutator: mutate
  };
};

export const UserInformationSocketListener = (userId) => {
  const websocketPath = `${process.env.NEXT_PUBLIC_WEB_SOCET_PREFIX}://${process.env.NEXT_PUBLIC_SOCKET_HOST_PORT}/game-update?userId=`;
  useSWRSubscription(websocketPath + userId,
    (key, { next }) => {
      const socket = new WebSocket(key);
      if (userId === null) {
        socket.close();
      }
      socket.addEventListener('message', (event) => {
        if (!event.data) {
          return;
        }
        const jsonData = JSON.parse(event.data);
        const { updateType } = jsonData;
        switch (updateType) {
          case UPDATE_TYPE.BET_INFORMATION:
            const {status} = jsonData
            updateBetInCache(jsonData);
            if (status === BET_STATUSES.CLOSED){
              toastMessage(GAME_FINISHED_MESSAGE, SUCCESS_ICON)
            }
            break;
          case UPDATE_TYPE.ACCOUNT_INFORMATION:
            const { ethBalance, totalEthProfitEarned } = jsonData;
            updateBalanceInfo({ethBalance, totalEthProfitEarned})
            toastMessage(TOAST_BALANCE_CHANGE_MESSAGE, INFO_ICON)
            break;
        }

      });
      socket.addEventListener('open', () => socket.send(userId));
      if (socket.readyState === 1) {
        socket.send(userId);
      }

      socket.addEventListener('error', (event) => next(event.error));
      return () => {
        if (socket.readyState === 1) {
          socket.close();
        }
      };
    });
};
export const EthUsdPrice = () => {
  const url = process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/common/price/usd';
  const priceFetcher = async (url) => await axios.get(url, getHeaders()).then(
    res => res.data).catch();
  const { data, error, isLoading, mutate } = useSWRImmutable(url, priceFetcher);
  return {
    ethUsdPrice: data,
    isLoading,
    isError: error,
    priceMutator: mutate
  };
};

export const getUserBets = async () => {
  const betsUrl = process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/common/bets';
  return await axios.get(betsUrl, getHeaders())
                    .then(res => {
                      updateBetsCache(res.data);
                      return res.data;
                    })
                    .catch(() => toastMessage(TOAST_GENERAL_ERROR_MESSAGE, ERROR_ICON));
};

export const createAdminBet = async (betsPayload) => {
  const betsUrl = process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/admin/bets';
  return await axios.post(betsUrl, betsPayload, getHeaders())
                    .then(res => {
                      if (res.data && res.data['betDto']) {
                        const newBet = res.data['betDto'];
                        const existingBets = getBetsFromCache();
                        updateBetsCache([newBet, ...existingBets]);
                      }
                      handleResponse(res);
                    })
                    .catch(() => toastMessage(TOAST_GENERAL_ERROR_MESSAGE, ERROR_ICON));
};

export const addUserBet = async (betId) => {
  const putUrl = process.env.NEXT_PUBLIC_BACKEND_URL
    + '/v1/all-bet/user/bets/'
    + getUserId()
    + '/'
    + betId;
  return await axios.put(putUrl, {}, getHeaders())
                    .then(res => {
                      const betDto = res.data['betDto'];
                      const currentBets = getBetsFromCache();
                      if (betDto) {
                        updateBetsCache([betDto, ...currentBets]);
                      }
                      handleResponse(res);
                    }).catch(() => toastMessage(TOAST_GENERAL_ERROR_MESSAGE, ERROR_ICON));
};

export const createUserTransaction = async (betId,  predictedOutcome) => {
  const postUrl = process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/user/create/transaction';


  if (getNotEnoughBalanceBet(betId)){
    toastMessage(BE_RESPONSES_MESSAGES.NOT_ENOUGH_BALANCE_ERROR, ERROR_ICON)
    return;
  }
  await axios.post(postUrl, {
    userId: getUserId(),
    betId,
    predictedOutcome
  }, getHeaders()).then(res => {

      const responseType = res['data']['responseType'];
      const message = BE_RESPONSES_MESSAGES[responseType];
      if (message === BE_RESPONSES_MESSAGES.NOT_ENOUGH_BALANCE_ERROR) {
        updateNotEnoughBalanceBet(betId)
      }
      handleResponse(res);
    }
  ).catch(() => toastMessage(BE_RESPONSES_MESSAGES.NOT_ENOUGH_BALANCE_ERROR, ERROR_ICON));
};

export const changeAdminBetStatus = async (accountBet) => {
  const betsUpdateStatusUrl = process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/admin/bets/status';
  return await axios.put(betsUpdateStatusUrl, accountBet, getHeaders())
                    .then(res => {
                      if (res.data) {
                        updateBetInCache(res.data);
                        return res.data;
                      }
                      toastMessage(BE_RESPONSES_MESSAGES.GENERAL_ERROR, ERROR_ICON);
                      return undefined;
                    })
                    .catch(() => {
                        toastMessage(BE_RESPONSES_MESSAGES.GENERAL_ERROR, ERROR_ICON);
                        return undefined;
                      }
                    );
};

export const removeBetFromUserUpdateCache = async (betId) => {
  const userId = getUserId()
  const unlinkBetUrl = process.env.NEXT_PUBLIC_BACKEND_URL
    + '/v1/all-bet/common/unlink/bet/'
    + userId
    + '/'
    + betId;
  await axios.put(unlinkBetUrl, {}, getHeaders())
             .then(() => {
               const betsFormCache = getBetsFromCache();
               updateBetsCache(betsFormCache.filter(bet => bet.id !== betId));
             })
             .catch(() => false);
};

export const linkWalletAddress = async () => {
  const linkWalletAddressUrl = process.env.NEXT_PUBLIC_BACKEND_URL + '/v1/all-bet/common/linkWallet'
  return await axios.put(linkWalletAddressUrl,{}, getHeaders() )
    .then(() => true).catch(() => {
        toastMessage(BE_RESPONSES_MESSAGES.INCORRECT_WALLET_ADDRESS, ERROR_ICON)
      return false
    })
}

const updateBetInCache = (changedBet) => {
  const currentBets = getBetsFromCache();
  const {
    id,
    status,
    outcome,
    ethWinStakeAmount,
    ethLossStakeAmount,
    estimatedEthAdminProfit,
    betsForWinCount,
    betsForLossCount,
    ethProfit,
    closesAt
  } = changedBet;
  const changedBets = currentBets.map(bet => ({
    ...bet,
    status: bet.id === id ? status : bet.status,
    outcome: bet.id === id ? outcome : bet.outcome,
    ethWinStakeAmount: bet.id === id ? ethWinStakeAmount : bet.ethWinStakeAmount,
    ethLossStakeAmount: bet.id === id ? ethLossStakeAmount : bet.ethLossStakeAmount,
    betsForWinCount: bet.id === id ? betsForWinCount : bet.betsForWinCount,
    betsForLossCount: bet.id === id ? betsForLossCount : bet.betsForLossCount,
    ethProfit: bet.id === id ? ethProfit : bet.ethProfit,
    closesAt: bet.id === id ? closesAt : bet.closesAt,
    estimatedEthAdminProfit: bet.id === id ? estimatedEthAdminProfit: bet.estimatedEthAdminProfit
  }));
  updateBetsCache(changedBets);
};