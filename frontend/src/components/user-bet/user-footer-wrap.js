import {Box, Stack} from "@mui/material";

export const UserFooterWrap = ({children}) => {
    return <Stack
        alignItems="center"
        direction="row"
        justifyContent="start"
        spacing={1}
        sx={{p: 2}}
    >
        <Box sx={{m: -1.5, display: 'flex', justifyContent: 'center'}}>
            {children}
        </Box>
    </Stack>


}