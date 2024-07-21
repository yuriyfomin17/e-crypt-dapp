import {Box, Divider, Stack, SvgIcon, Typography} from "@mui/material";
import Tooltip from "@mui/material/Tooltip";
import MonetizationOnIcon from "@mui/icons-material/MonetizationOn";
import { convertToUsdt } from '../../helpers/helper-functions';
import { EthUsdPrice } from '../../helpers/helper-rest-functions';

export const AdminFooterWrap = ({children,  estimatedEthAdminProfit}) => {
  const { ethUsdPrice } = EthUsdPrice();

    return<>
        <Divider color="grey"
                 sx={{ height: 2, width: '100%' }}/>
        <Stack
            alignItems="center"
            direction="row"
            justifyContent="space-between"
            spacing={1}
            sx={{ p: 2 }}
        >
            <Box display='flex'
                 justifyItems='center'
                 alignItems='center'
            >
                <Tooltip title="eth Profit">
                    <SvgIcon
                        color="action"
                        fontSize="large"
                    >
                        <MonetizationOnIcon color="success"/>
                    </SvgIcon>
                </Tooltip>
                <Typography color="text.primary"
                            variant="h6">
                    Est. profit {estimatedEthAdminProfit} eth ≈  {convertToUsdt(estimatedEthAdminProfit, ethUsdPrice)} usd
                </Typography>
            </Box>
            {children}
        </Stack>
    </>
}