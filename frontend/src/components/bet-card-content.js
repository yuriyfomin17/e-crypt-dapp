import {Box, CardContent, SvgIcon, Typography} from '@mui/material';
import Tooltip from '@mui/material/Tooltip';
import PeopleIcon from '@mui/icons-material/People';
import LinearProgress from '@mui/material/LinearProgress';
import MonetizationOnIcon from '@mui/icons-material/MonetizationOn';
import {EthUsdPrice} from '../helpers/helper-rest-functions';
import { calculatePrize, convertToFloatStats, convertToUsdt } from '../helpers/helper-functions';
import {USER_PROFIT_COEFFICIENT} from '../helpers/helper-constants';

const getPercentage = (numerator, total) => {
    if (total === 0) {
        return 0;
    }
    return Math.round(100 * (numerator / total));
};

const betsCountComp = (count) => (
    <Box sx={{display: 'flex', alignItems: 'center', margin: "5px"}}>
        <Tooltip title="Bets count">
            <SvgIcon fontSize="medium">
                <PeopleIcon color="success"/>
            </SvgIcon>
        </Tooltip>
        <Typography fontSize="large"
                    color="text.primary">
            {count}
        </Typography>
    </Box>
);

const prizeComp = (usdPrize) => (
    <Box sx={{display: 'flex', alignItems: 'center', margin: "5px"}}>
        <Tooltip title="Average prize">
            <SvgIcon fontSize="medium">
                <MonetizationOnIcon color="success"/>
            </SvgIcon>
        </Tooltip>
        <Typography fontSize="large">
            {usdPrize} usd
        </Typography>
    </Box>
);

const percentageContent = (choice, percentage, location) => (
    <>
        <Box alignSelf={location}>
            <Typography variant="h5">
                {choice}
            </Typography>
        </Box>
        <Box>
            <Typography variant="h2">
                {percentage}%
            </Typography>
        </Box>
        <LinearProgress variant="determinate"
                        value={percentage}/>
    </>
);
export const BetCardContent = ({currentBet}) => {
    const {
        ethWinStakeAmount,
        ethLossStakeAmount,
        betsForWinCount,
        betsForLossCount,
        ethTicketPrice
    } = convertToFloatStats(currentBet);
    const {ethUsdPrice} = EthUsdPrice();
    const total = betsForWinCount + betsForLossCount
    const positivePercentage = getPercentage(betsForWinCount, total);
    const negativePercentage = getPercentage(betsForLossCount, total);
    const usdtWinStakeAmount = convertToUsdt(ethWinStakeAmount, ethUsdPrice)
    const ethTicketPriceUsdt = convertToUsdt(ethTicketPrice, ethUsdPrice)
    const usdtLossStakeAmount = convertToUsdt(ethLossStakeAmount, ethUsdPrice)
    return (
        <CardContent>
            <Box sx={{m: -1.5, display: 'flex', justifyContent: 'center'}}>
                <Box sx={{
                    p: 2,
                    width: '100%',
                    display: 'flex',
                    justifyContent: 'space-between',
                    borderRight: '1px solid'
                }}>
                    <Box sx={{display: 'flex', flexDirection: 'column'}}>
                        {prizeComp(calculatePrize(usdtLossStakeAmount * USER_PROFIT_COEFFICIENT, betsForWinCount, total, ethTicketPriceUsdt))}
                        {betsCountComp(betsForWinCount)}
                    </Box>
                    <Box sx={{display: 'flex', flexDirection: 'column'}}>

                        {percentageContent('Yes', positivePercentage, 'end')}
                    </Box>
                </Box>


                <Box sx={{
                    p: 2,
                    width: '100%',
                    borderRadius: '10px',
                    display: 'flex',
                    justifyContent: 'space-between'
                }}>
                    <Box sx={{display: 'flex', flexDirection: 'column'}}>
                        {percentageContent('No', negativePercentage, 'start')}
                    </Box>
                    <Box sx={{display: 'flex', flexDirection: 'column'}}>
                        {prizeComp(calculatePrize(usdtWinStakeAmount * USER_PROFIT_COEFFICIENT, betsForLossCount, total, ethTicketPriceUsdt))}
                        {betsCountComp(betsForLossCount)}
                    </Box>
                </Box>
            </Box>
        </CardContent>
    );
};